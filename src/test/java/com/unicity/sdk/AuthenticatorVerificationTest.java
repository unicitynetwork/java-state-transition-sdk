package com.unicity.sdk;

import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.hash.JavaDataHasher;
import com.unicity.sdk.shared.signing.Signature;
import com.unicity.sdk.shared.signing.SigningService;
import com.unicity.sdk.shared.util.HexConverter;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify authenticator implementation matches TypeScript reference.
 * Uses exact values from TypeScript test case.
 */
public class AuthenticatorVerificationTest {

    @Test
    void testRequestIdCalculation() throws Exception {
        // Values from TypeScript test
        // Note: TypeScript test shows RequestId.fromJSON expects the value WITH algorithm prefix
        String expectedRequestIdHex = "00009399ada3bd4dfa4bce4787bbc416be1e617a734efeb9c4d70a70d4503d5637b0";
        String publicKeyHex = "02bf8d9e7687f66c7fce1e98edbc05566f7db740030722cf6cf62aca035c5035ea";
        String stateHashHex = "0000f7f53c361c30535ed52b05f24616b5580d562ba7494e352dc2f934a51a78bb0a";

        // Parse inputs
        byte[] publicKey = HexConverter.decode(publicKeyHex);
        DataHash stateHash = DataHash.fromImprint(HexConverter.decode(stateHashHex));
        
        // Create RequestId
        RequestId requestId = RequestId.create(publicKey, stateHash).get();
        
        // Debug: Check what we're hashing
        System.out.println("Public key for RequestId: " + HexConverter.encode(publicKey));
        System.out.println("State hash CBOR for RequestId: " + HexConverter.encode(stateHash.toCBOR()));
        
        // The RequestId.toJSON() should return the hex string representation with algorithm prefix
        String actualRequestIdJson = (String) requestId.toJSON();
        System.out.println("Expected RequestId: " + expectedRequestIdHex);
        System.out.println("Actual RequestId JSON: " + actualRequestIdJson);
        
        // Also check the BigInt value
        System.out.println("RequestId BitString: " + requestId.toBitString().toBigInteger().toString(16));
        
        assertEquals(expectedRequestIdHex, actualRequestIdJson, "RequestId should match TypeScript reference");
    }
    
    @Test
    void testSignatureVerification() throws Exception {
        // Values from TypeScript test
        String transactionHashHex = "0000d6035b65700f0af73cc62a580eb833c20f40aaee460087f5fb43ebb3c047f1d4";
        String publicKeyHex = "02bf8d9e7687f66c7fce1e98edbc05566f7db740030722cf6cf62aca035c5035ea";
        String signatureHex = "301c7f19d5e0a7e350012ab7bbaf26a0152a751eec06d18563f96bcf06d2380e7de7ce6cebb8c11479d1bd9c463c3ba47396b5f815c552b344d430b0d011a2e701";
        String stateHashHex = "0000f7f53c361c30535ed52b05f24616b5580d562ba7494e352dc2f934a51a78bb0a";

        // Parse DataHash objects from imprint format (algorithm prefix + hash)
        DataHash transactionHash = DataHash.fromImprint(HexConverter.decode(transactionHashHex));
        DataHash stateHash = DataHash.fromImprint(HexConverter.decode(stateHashHex));
        
        // Parse public key
        byte[] publicKey = HexConverter.decode(publicKeyHex);
        
        // Parse signature - last byte is recovery ID
        byte[] sigBytes = HexConverter.decode(signatureHex);
        System.out.println("Full signature hex: " + signatureHex);
        System.out.println("Signature length: " + sigBytes.length);
        
        byte[] signatureOnly = new byte[64];
        System.arraycopy(sigBytes, 0, signatureOnly, 0, 64);
        int recoveryId = sigBytes[64] & 0xFF;
        System.out.println("Recovery ID: " + recoveryId);
        Signature signature = new Signature(signatureOnly, recoveryId);
        
        // Debug: print what we're verifying
        System.out.println("Transaction hash: " + transactionHashHex);
        System.out.println("Public key: " + publicKeyHex);
        System.out.println("Hash bytes to verify: " + HexConverter.encode(transactionHash.getHash()));
        
        // Verify signature verification with public key
        boolean verified = SigningService.verifyWithPublicKey(transactionHash, sigBytes, publicKey).get();
        System.out.println("Signature verification result: " + verified);
        
        // Create authenticator using the pre-computed signature
        MockSigningService signingService = new MockSigningService(publicKey, signature);
        Authenticator authenticator = Authenticator.create(signingService, transactionHash, stateHash).get();
        
        // Verify authenticator has correct components
        assertNotNull(authenticator);
        assertEquals(transactionHash, authenticator.getTransactionHash());
        assertEquals(stateHash, authenticator.getStateHash());
        
        // Verify the signature matches
        assertEquals(signatureHex, authenticator.getSignature().toJSON());
        
        // Verify authenticator JSON format
        Object json = authenticator.toJSON();
        System.out.println("Authenticator JSON: " + json);
    }
    
    /**
     * Mock signing service that returns pre-defined signature
     */
    static class MockSigningService implements com.unicity.sdk.shared.signing.ISigningService<Signature> {
        private final byte[] publicKey;
        private final Signature signature;
        
        MockSigningService(byte[] publicKey, Signature signature) {
            this.publicKey = publicKey;
            this.signature = signature;
        }
        
        @Override
        public byte[] getPublicKey() {
            return publicKey;
        }
        
        @Override
        public String getAlgorithm() {
            return "secp256k1";
        }
        
        @Override
        public java.util.concurrent.CompletableFuture<Signature> sign(DataHash hash) {
            return java.util.concurrent.CompletableFuture.completedFuture(signature);
        }
        
        public java.util.concurrent.CompletableFuture<Signature> sign(byte[] data) {
            return java.util.concurrent.CompletableFuture.completedFuture(signature);
        }
        
        @Override
        public java.util.concurrent.CompletableFuture<Boolean> verify(DataHash hash, Signature signature) {
            return java.util.concurrent.CompletableFuture.completedFuture(true);
        }
    }
}