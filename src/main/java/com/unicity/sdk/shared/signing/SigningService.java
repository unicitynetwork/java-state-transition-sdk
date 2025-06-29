package com.unicity.sdk.shared.signing;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.hash.JavaDataHasher;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.crypto.params.ECKeyParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;

/**
 * Default signing service.
 */
public class SigningService implements ISigningService<Signature> {
    private static final String CURVE_NAME = "secp256k1";
    
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
    private final byte[] privateKey;
    private final byte[] publicKey;
    private final ECDomainParameters domainParams;

    /**
     * Signing service constructor.
     * @param privateKey private key bytes.
     */
    public SigningService(byte[] privateKey) {
        this.privateKey = Arrays.copyOf(privateKey, privateKey.length);
        
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME);
        this.domainParams = new ECDomainParameters(
            ecSpec.getCurve(), 
            ecSpec.getG(), 
            ecSpec.getN(), 
            ecSpec.getH()
        );
        
        // Calculate public key
        ECPoint Q = ecSpec.getG().multiply(new BigInteger(1, privateKey));
        this.publicKey = Q.getEncoded(true); // compressed format
    }

    @Override
    public byte[] getPublicKey() {
        return Arrays.copyOf(publicKey, publicKey.length);
    }
    
    @Override
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
    public static CompletableFuture<SigningService> createFromSecret(byte[] secret, byte[] nonce) {
        JavaDataHasher hasher = new JavaDataHasher(HashAlgorithm.SHA256);
        hasher.update(secret);
        if (nonce != null) {
            hasher.update(nonce);
        }
        
        return hasher.digest().thenApply(hash -> new SigningService(hash.getHash()));
    }

    @Override
    public CompletableFuture<Signature> sign(DataHash hash) {
        return sign(hash.getHash());
    }

    @Override
    public CompletableFuture<Signature> sign(byte[] data) {
        try {
            ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(
                new BigInteger(1, privateKey), 
                domainParams
            );
            
            ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
            signer.init(true, privKey);
            
            BigInteger[] signature = signer.generateSignature(data);
            BigInteger r = signature[0];
            BigInteger s = signature[1];
            
            // Ensure s is in the lower half of the order (malleability fix)
            BigInteger halfN = domainParams.getN().shiftRight(1);
            if (s.compareTo(halfN) > 0) {
                s = domainParams.getN().subtract(s);
            }
            
            // Convert to compact format (64 bytes)
            byte[] rBytes = toFixedLength(r, 32);
            byte[] sBytes = toFixedLength(s, 32);
            
            byte[] sigBytes = new byte[64];
            System.arraycopy(rBytes, 0, sigBytes, 0, 32);
            System.arraycopy(sBytes, 0, sigBytes, 32, 32);
            
            // Calculate recovery ID (yuck)
            int recoveryId = 0;
            for (int i = 0; i < 4; i++) {
                try {
                    ECPoint recovered = recoverFromSignature(i, r, s, data);
                    if (recovered != null && Arrays.equals(recovered.getEncoded(true), publicKey)) {
                        recoveryId = i;
                        break;
                    }
                } catch (Exception ex) {
                    // Try next recovery ID
                }
            }
            
            return CompletableFuture.completedFuture(new Signature(sigBytes, recoveryId));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Boolean> verify(DataHash hash, Signature signature) {
        return verifyWithPublicKey(hash, signature.getBytes(), this.publicKey);
    }

    /**
     * Verify signature with public key.
     */
    public static CompletableFuture<Boolean> verifyWithPublicKey(DataHash hash, byte[] signature, byte[] publicKey) {
        try {
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME);
            ECDomainParameters domainParams = new ECDomainParameters(
                ecSpec.getCurve(), 
                ecSpec.getG(), 
                ecSpec.getN(), 
                ecSpec.getH()
            );
            
            ECPoint pubPoint = ecSpec.getCurve().decodePoint(publicKey);
            ECPublicKeyParameters pubKey = new ECPublicKeyParameters(pubPoint, domainParams);
            
            ECDSASigner verifier = new ECDSASigner();
            verifier.init(false, pubKey);
            
            // Extract r and s from compact signature (first 64 bytes)
            BigInteger r = new BigInteger(1, Arrays.copyOfRange(signature, 0, 32));
            BigInteger s = new BigInteger(1, Arrays.copyOfRange(signature, 32, 64));
            
            boolean valid = verifier.verifySignature(hash.getHash(), r, s);
            return CompletableFuture.completedFuture(valid);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
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
    private ECPoint recoverFromSignature(int recId, BigInteger r, BigInteger s, byte[] message) {
        BigInteger n = domainParams.getN();
        BigInteger e = new BigInteger(1, message);
        BigInteger x = r;
        
        if (recId >= 2) {
            x = x.add(n);
        }
        
        ECCurve curve = domainParams.getCurve();
        
        // Calculate y from x
        ECPoint R = decompressKey(x, (recId & 1) == 1, curve);
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
        ECPoint point2 = domainParams.getG().multiply(e);
        ECPoint Q = point1.subtract(point2).multiply(rInv);
        
        return Q;
    }
    
    
    /**
     * Decompress a compressed public key point.
     */
    private ECPoint decompressKey(BigInteger x, boolean yBit, ECCurve curve) {
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
    public static CompletableFuture<Boolean> verifySignatureWithRecoveredPublicKey(DataHash hash, Signature signature) {
        try {
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME);
            ECDomainParameters domainParams = new ECDomainParameters(
                ecSpec.getCurve(), 
                ecSpec.getG(), 
                ecSpec.getN(), 
                ecSpec.getH()
            );
            
            // Extract r and s from signature
            BigInteger r = new BigInteger(1, Arrays.copyOfRange(signature.getBytes(), 0, 32));
            BigInteger s = new BigInteger(1, Arrays.copyOfRange(signature.getBytes(), 32, 64));
            BigInteger e = new BigInteger(1, hash.getHash());
            
            // Recover public key
            BigInteger x = r;
            if (signature.getRecovery() >= 2) {
                x = x.add(domainParams.getN());
            }
            
            ECCurve curve = domainParams.getCurve();
            
            // Decompress point
            byte[] compEnc = new byte[33];
            compEnc[0] = (byte) ((signature.getRecovery() & 1) == 1 ? 0x03 : 0x02);
            byte[] xBytes = x.toByteArray();
            if (xBytes.length > 32) {
                System.arraycopy(xBytes, xBytes.length - 32, compEnc, 1, 32);
            } else {
                System.arraycopy(xBytes, 0, compEnc, 33 - xBytes.length, xBytes.length);
            }
            
            ECPoint R = curve.decodePoint(compEnc);
            if (R == null || !R.isValid()) {
                return CompletableFuture.completedFuture(false);
            }
            
            // Calculate public key: Q = r^-1 * (s*R - e*G)
            BigInteger rInv = r.modInverse(domainParams.getN());
            ECPoint point1 = R.multiply(s);
            ECPoint point2 = domainParams.getG().multiply(e);
            ECPoint Q = point1.subtract(point2).multiply(rInv);
            
            if (!Q.isValid()) {
                return CompletableFuture.completedFuture(false);
            }
            
            // Verify signature with recovered public key
            return verifyWithPublicKey(hash, signature.getBytes(), Q.getEncoded(true));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}