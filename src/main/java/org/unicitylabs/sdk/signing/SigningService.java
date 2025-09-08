package org.unicitylabs.sdk.signing;

import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Default signing service.
 */
public class SigningService {

  private static final String CURVE_NAME = "secp256k1";
  private static final ECParameterSpec EC_SPEC = ECNamedCurveTable.getParameterSpec(CURVE_NAME);
  private static final ECDomainParameters EC_DOMAIN_PARAMETERS = new ECDomainParameters(
      EC_SPEC.getCurve(),
      EC_SPEC.getG(),
      EC_SPEC.getN(),
      EC_SPEC.getH()
  );

  private final ECPrivateKeyParameters privateKey;
  private final byte[] publicKey;

  /**
   * Signing service constructor.
   *
   * @param privateKey private key bytes.
   */
  public SigningService(byte[] privateKey) {
    if (privateKey == null) {
      throw new IllegalArgumentException("privateKey cannot be null");
    }

    BigInteger privateKeyAsBigInt = new BigInteger(1, privateKey);
    if (privateKeyAsBigInt.compareTo(BigInteger.ONE) < 0
        || privateKeyAsBigInt.compareTo(EC_SPEC.getN()) >= 0) {
      throw new IllegalArgumentException("Invalid private key: must be in range [1, N)");
    }

    // Calculate public key
    ECPoint Q = EC_SPEC.getG().multiply(privateKeyAsBigInt);
    this.publicKey = Q.getEncoded(true); // compressed format
    this.privateKey = new ECPrivateKeyParameters(
        privateKeyAsBigInt,
        EC_DOMAIN_PARAMETERS
    );
  }

  public byte[] getPublicKey() {
    return Arrays.copyOf(publicKey, publicKey.length);
  }

  public String getAlgorithm() {
    return "secp256k1";
  }

  /**
   * Generate a random private key.
   */
  public static byte[] generatePrivateKey() {
    SecureRandom random = new SecureRandom();
    byte[] privateKey = new byte[32];
    random.nextBytes(privateKey);
    return privateKey;
  }

  /**
   * Create signing service from secret and optional nonce.
   */
  public static SigningService createFromSecret(byte[] secret, byte[] nonce) {
    DataHasher hasher = new DataHasher(HashAlgorithm.SHA256);
    hasher.update(secret);
    if (nonce != null) {
      hasher.update(nonce);
    }

    return new SigningService(hasher.digest().getData());
  }

  public Signature sign(DataHash hash) {
    ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
    signer.init(true, this.privateKey);

    BigInteger[] signature = signer.generateSignature(hash.getData());
    BigInteger r = signature[0];
    BigInteger s = signature[1];

    // Ensure s is in the lower half of the order (malleability fix)
    BigInteger halfN = EC_DOMAIN_PARAMETERS.getN().shiftRight(1);
    if (s.compareTo(halfN) > 0) {
      s = EC_DOMAIN_PARAMETERS.getN().subtract(s);
    }

    byte[] signatureBytes = new byte[64];
    System.arraycopy(toFixedLength(r, 32), 0, signatureBytes, 0, 32);
    System.arraycopy(toFixedLength(s, 32), 0, signatureBytes, 32, 32);

    // Calculate recovery ID
    int recoveryId = 0;
    for (int i = 0; i < 4; i++) {
      try {
        ECPoint recovered = recoverFromSignature(i, r, s, hash.getData());
        if (recovered != null && Arrays.equals(recovered.getEncoded(true), publicKey)) {
          recoveryId = i;
          break;
        }
      } catch (Exception ex) {
        // Try next recovery ID
      }
    }

    return new Signature(signatureBytes, recoveryId);
  }

  public boolean verify(DataHash hash, Signature signature) {
    return verifyWithPublicKey(hash, signature.getBytes(), this.publicKey);
  }

  /**
   * Verify signature with public key.
   */
  public static boolean verifyWithPublicKey(DataHash hash, byte[] signature, byte[] publicKey) {
    ECPoint pubPoint = EC_SPEC.getCurve().decodePoint(publicKey);
    ECPublicKeyParameters pubKey = new ECPublicKeyParameters(pubPoint, EC_DOMAIN_PARAMETERS);

    ECDSASigner verifier = new ECDSASigner();
    verifier.init(false, pubKey);

    // Extract r and s from compact signature (first 64 bytes)
    BigInteger r = new BigInteger(1, Arrays.copyOfRange(signature, 0, 32));
    BigInteger s = new BigInteger(1, Arrays.copyOfRange(signature, 32, 64));

    return verifier.verifySignature(hash.getData(), r, s);
  }


  private byte[] toFixedLength(BigInteger value, int length) {
    byte[] bytes = value.toByteArray();
    if (bytes.length == length) {
      return bytes;
    }

    byte[] result = new byte[length];
    if (bytes.length > length) {
      // Remove leading zero if present
      System.arraycopy(bytes, bytes.length - length, result, 0, length);
    } else {
      // Pad with zeros
      System.arraycopy(bytes, 0, result, length - bytes.length, bytes.length);
    }
    return result;
  }

  /**
   * Recover public key from signature for a specific recovery ID.
   */
  private static ECPoint recoverFromSignature(int recId, BigInteger r, BigInteger s,
      byte[] message) {
    BigInteger n = EC_DOMAIN_PARAMETERS.getN();
    BigInteger e = new BigInteger(1, message);
    BigInteger x = r;

    if (recId >= 2) {
      x = x.add(n);
    }

    ECCurve curve = EC_DOMAIN_PARAMETERS.getCurve();

    // Calculate y from x
    ECPoint R = SigningService.decompressKey(x, (recId & 1) == 1, curve);
    if (R == null) {
      return null;
    }

    // Verify R is on curve and has order n
    if (!R.isValid() || !R.multiply(n).isInfinity()) {
      return null;
    }

    // Calculate public key: Q = r^-1 * (s*R - e*G)
    BigInteger rInv = r.modInverse(n);
    ECPoint point1 = R.multiply(s);
    ECPoint point2 = EC_DOMAIN_PARAMETERS.getG().multiply(e);
    return point1.subtract(point2).multiply(rInv);
  }


  /**
   * Decompress a compressed public key point.
   */
  private static ECPoint decompressKey(BigInteger x, boolean yBit, ECCurve curve) {
    try {
      byte[] compEnc = new byte[33];
      compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
      byte[] xBytes = x.toByteArray();
      if (xBytes.length > 32) {
        System.arraycopy(xBytes, xBytes.length - 32, compEnc, 1, 32);
      } else {
        System.arraycopy(xBytes, 0, compEnc, 33 - xBytes.length, xBytes.length);
      }
      return curve.decodePoint(compEnc);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Verify signature with recovered public key - extract public key from signature.
   */
  public static boolean verifySignatureWithRecoveredPublicKey(DataHash hash, Signature signature) {
    // Extract r and s from signature
    BigInteger r = new BigInteger(1, Arrays.copyOfRange(signature.getBytes(), 0, 32));
    BigInteger s = new BigInteger(1, Arrays.copyOfRange(signature.getBytes(), 32, 64));

    ECPoint recovered = recoverFromSignature(signature.getRecovery(), r, s, hash.getData());
    if (recovered == null || !recovered.isValid()) {
      return false;
    }

    return verifyWithPublicKey(hash, signature.getBytes(), recovered.getEncoded(true));
  }
}