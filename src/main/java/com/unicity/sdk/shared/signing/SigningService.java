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
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;

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
        try {
            ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(
                new BigInteger(1, privateKey), 
                domainParams
            );
            
            ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
            signer.init(true, privKey);
            
            BigInteger[] signature = signer.generateSignature(hash.getHash());
            
            // Convert to compact format (64 bytes)
            byte[] r = toFixedLength(signature[0], 32);
            byte[] s = toFixedLength(signature[1], 32);
            
            byte[] sigBytes = new byte[64];
            System.arraycopy(r, 0, sigBytes, 0, 32);
            System.arraycopy(s, 0, sigBytes, 32, 32);
            
            // For now, we'll use recovery id 0. In a full implementation,
            // we would calculate the correct recovery id
            int recovery = 0;
            
            return CompletableFuture.completedFuture(new Signature(sigBytes, recovery));
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
            
            // Extract r and s from compact signature
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
}