package org.unicitylabs.sdk.signing;

import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

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
        
        SigningService signingService = SigningService.createFromMaskedSecret(secret, nonce);
        
        assertNotNull(signingService);
        assertNotNull(signingService.getPublicKey());
        assertEquals("secp256k1", signingService.getAlgorithm());
    }
    
    @Test
    public void testSignAndVerify() throws Exception {
        byte[] privateKey = SigningService.generatePrivateKey();
        SigningService service = new SigningService(privateKey);
        
        // Create a test hash
        byte[] testData = "test data".getBytes(StandardCharsets.UTF_8);
        DataHash hash = new DataHash(HashAlgorithm.SHA256, testData);
        
        // Sign the hash
        Signature signature = service.sign(hash);
        
        assertNotNull(signature);
        assertEquals(64, signature.getBytes().length);
        
        // Verify the signature
        boolean isValid = service.verify(hash, signature);
        
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
        Signature signature = service.sign(hash);
        
        // Verify with public key
        boolean isValid = SigningService.verifyWithPublicKey(hash, signature.getBytes(), publicKey);
        
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
        boolean isValid = service.verify(hash, signature);
        
        assertFalse(isValid);
    }
}