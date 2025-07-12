package com.unicity.sdk;

import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.predicate.UnmaskedPredicate;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.signing.SigningService;
import com.unicity.sdk.shared.util.HexConverter;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.token.fungible.CoinId;
import com.unicity.sdk.transaction.MintTransactionData;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify mint transaction authenticator creation matches TypeScript.
 */
public class MintTransactionAuthenticatorTest {
    
    @Test
    void testMintTransactionAuthenticator() throws Exception {
        // Create a simple mint transaction
        byte[] secret = "test-secret".getBytes(StandardCharsets.UTF_8);
        byte[] nonce = new byte[32];
        Arrays.fill(nonce, (byte) 1);
        
        // Create signing service
        SigningService signingService = SigningService.createFromSecret(secret, nonce).get();
        System.out.println("Public key: " + HexConverter.encode(signingService.getPublicKey()));
        
        // Create token components
        TokenId tokenId = TokenId.create(nonce);
        TokenType tokenType = TokenType.create(nonce);
        
        // Create predicate using factory method
        UnmaskedPredicate predicate = UnmaskedPredicate.create(
            tokenId,
            tokenType,
            signingService,
            HashAlgorithm.SHA256,
            nonce
        ).get();
        
        // Create address
        DirectAddress address = DirectAddress.create(predicate.getReference()).get();
        
        // Create coin data
        Map<CoinId, BigInteger> coins = new HashMap<>();
        coins.put(new CoinId(new byte[]{0}), BigInteger.valueOf(1000));
        TokenCoinData coinData = new TokenCoinData(coins);
        
        // Create empty token data
        ISerializable emptyTokenData = new ISerializable() {
            @Override
            public Object toJSON() {
                return "";
            }
            
            @Override
            public byte[] toCBOR() {
                return new byte[0];
            }
        };
        
        // Create data hash
        byte[] data = "test-data".getBytes(StandardCharsets.UTF_8);
        DataHash dataHash = DataHasher.digest(HashAlgorithm.SHA256, data);
        
        // Create salt
        byte[] salt = new byte[32];
        Arrays.fill(salt, (byte) 2);
        
        // Create mint transaction data
        MintTransactionData<?> mintData = new MintTransactionData<>(
            tokenId,
            tokenType,
            predicate,
            emptyTokenData,
            coinData,
            dataHash,
            salt
        );
        
        // Debug: Print mint transaction details
        System.out.println("\nMint Transaction Details:");
        System.out.println("Token ID: " + tokenId.toJSON());
        System.out.println("Token Type: " + tokenType.toJSON());
        System.out.println("Recipient: " + address.getAddress());
        System.out.println("Data hash: " + dataHash.toJSON());
        System.out.println("Transaction hash: " + mintData.getHash().toJSON());
        System.out.println("Source state: " + mintData.getSourceState().toJSON());
        System.out.println("Source state hash: " + mintData.getSourceState().getHash().toJSON());
        
        // Create RequestId for commitment
        RequestId requestId = RequestId.create(signingService.getPublicKey(), mintData.getSourceState().getHash()).get();
        System.out.println("\nRequestId: " + requestId.toJSON());
        
        // Create authenticator
        Authenticator authenticator = Authenticator.create(
            signingService,
            mintData.getHash(),
            mintData.getSourceState().getHash()
        ).get();
        
        System.out.println("\nAuthenticator Details:");
        System.out.println("Transaction hash: " + authenticator.getTransactionHash().toJSON());
        System.out.println("State hash: " + authenticator.getStateHash().toJSON());
        System.out.println("Signature: " + authenticator.getSignature().toJSON());
        System.out.println("Public key: " + HexConverter.encode(authenticator.getPublicKey()));
        
        // Verify the authenticator can verify the transaction hash
        boolean verified = authenticator.verify(mintData.getHash()).get();
        System.out.println("\nAuthenticator verification: " + verified);
        assertTrue(verified, "Authenticator should verify the transaction hash");
        
        // Print authenticator JSON for debugging
        System.out.println("\nAuthenticator JSON: " + authenticator.toJSON());
    }
}