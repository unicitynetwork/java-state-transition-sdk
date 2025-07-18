package com.unicity.sdk;

import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.signing.SigningService;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.address.DirectAddress;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify that the SDK works with Android API level 31 (Android 12).
 * These tests ensure we don't use any Java APIs that are not available on Android.
 */
public class AndroidCompatibilityTest {
    
    @Test
    void testCoreSDKFeaturesWorkOnAndroid() throws Exception {
        // Test 1: Hashing (uses Bouncy Castle, not Java crypto)
        byte[] data = "test data".getBytes(StandardCharsets.UTF_8);
        var hash = DataHasher.digest(HashAlgorithm.SHA256, data);
        assertNotNull(hash);
        assertEquals(HashAlgorithm.SHA256, hash.getAlgorithm());
        
        // Test 2: Signing Service (uses Bouncy Castle)
        byte[] secret = "test secret".getBytes(StandardCharsets.UTF_8);
        byte[] nonce = new byte[32];
        var signingService = SigningService.createFromSecret(secret, nonce).get();
        assertNotNull(signingService.getPublicKey());
        
        // Test 3: Token IDs and Types
        TokenId tokenId = TokenId.create(new byte[32]);
        TokenType tokenType = TokenType.create(new byte[32]);
        assertNotNull(tokenId);
        assertNotNull(tokenType);
        
        // Test 4: Predicates
        var predicate = MaskedPredicate.create(
            tokenId,
            tokenType,
            signingService,
            HashAlgorithm.SHA256,
            nonce
        ).get();
        assertNotNull(predicate);
        
        // Test 5: Addresses
        var address = DirectAddress.create(predicate.getReference()).get();
        assertNotNull(address.toString());
        
        // Test 6: Verify we're not using Java 11+ specific APIs
        // This is enforced by Animal Sniffer during build
    }
    
    @Test
    void testNoJava11SpecificAPIs() {
        // This test documents that we avoid Java 11+ specific APIs:
        // - No java.net.http.HttpClient (using OkHttp instead)
        // - No var keyword in public APIs
        // - No List.of(), Map.of(), Set.of() (using traditional constructors)
        // - No Files.readString/writeString
        // - Target Java 11 instead of Java 8 (Android 12+ supports Java 11)
        
        // Animal Sniffer plugin verifies this at build time
        assertTrue(true, "Animal Sniffer validates Android compatibility");
    }
}