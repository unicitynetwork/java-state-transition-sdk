package com.unicity.sdk.shared.signing;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class SigningServiceTest {
    
    @Test
    public void testGeneratePrivateKey() {
        byte[] privateKey = SigningService.generatePrivateKey();
        
        assertNotNull(privateKey);
        assertEquals(32, privateKey.length);
        
        // Test that we can create a signing service with it
        SigningService service = new SigningService(privateKey);
        assertNotNull(service.getPublicKey());
        assertEquals(33, service.getPublicKey().length); // Compressed public key
    }
    
    @Test
    public void testCreateFromSecret() throws Exception {
        byte[] secret = "test secret".getBytes(StandardCharsets.UTF_8);
        byte[] nonce = "test nonce".getBytes(StandardCharsets.UTF_8);
        
        CompletableFuture<SigningService> future = SigningService.createFromSecret(secret, nonce);
        SigningService service = future.get();
        
        assertNotNull(service);
        assertNotNull(service.getPublicKey());
        assertEquals("secp256k1", service.getAlgorithm());
    }
    
    @Test
    public void testSignAndVerify() throws Exception {
        byte[] privateKey = SigningService.generatePrivateKey();
        SigningService service = new SigningService(privateKey);
        
        // Create a test hash
        byte[] testData = "test data".getBytes(StandardCharsets.UTF_8);
        DataHash hash = new DataHash(HashAlgorithm.SHA256, testData);
        
        // Sign the hash
        CompletableFuture<Signature> signFuture = service.sign(hash);
        Signature signature = signFuture.get();
        
        assertNotNull(signature);
        assertEquals(64, signature.getBytes().length);
        
        // Verify the signature
        CompletableFuture<Boolean> verifyFuture = service.verify(hash, signature);
        Boolean isValid = verifyFuture.get();
        
        assertTrue(isValid);
    }
    
    @Test
    public void testVerifyWithPublicKey() throws Exception {
        byte[] privateKey = SigningService.generatePrivateKey();
        SigningService service = new SigningService(privateKey);
        byte[] publicKey = service.getPublicKey();
        
        // Create a test hash
        byte[] testData = "test data".getBytes(StandardCharsets.UTF_8);
        DataHash hash = new DataHash(HashAlgorithm.SHA256, testData);
        
        // Sign the hash
        Signature signature = service.sign(hash).get();
        
        // Verify with public key
        CompletableFuture<Boolean> verifyFuture = SigningService.verifyWithPublicKey(hash, signature.getBytes(), publicKey);
        Boolean isValid = verifyFuture.get();
        
        assertTrue(isValid);
    }
    
    @Test
    public void testInvalidSignature() throws Exception {
        byte[] privateKey = SigningService.generatePrivateKey();
        SigningService service = new SigningService(privateKey);
        
        // Create a test hash
        byte[] testData = "test data".getBytes(StandardCharsets.UTF_8);
        DataHash hash = new DataHash(HashAlgorithm.SHA256, testData);
        
        // Create an invalid signature
        byte[] invalidSig = new byte[64];
        Signature signature = new Signature(invalidSig, 0);
        
        // Verify the signature
        CompletableFuture<Boolean> verifyFuture = service.verify(hash, signature);
        Boolean isValid = verifyFuture.get();
        
        assertFalse(isValid);
    }
    
    @Test
    public void testSignatureSerialization() throws Exception {
        byte[] privateKey = SigningService.generatePrivateKey();
        SigningService service = new SigningService(privateKey);
        
        byte[] testData = "test data".getBytes(StandardCharsets.UTF_8);
        DataHash hash = new DataHash(HashAlgorithm.SHA256, testData);
        
        Signature signature = service.sign(hash).get();
        
        // Test CBOR serialization
        byte[] cbor = signature.toCBOR();
        assertNotNull(cbor);
        assertTrue(cbor.length > 65); // Should be CBOR encoded byte string
    }
}