package org.unicitylabs.sdk.utils;

import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;

import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.api.SubmitCommitmentStatus;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.MaskedPredicate;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.transaction.*;
import org.unicitylabs.sdk.util.InclusionProofUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for tests.
 */
public class TestUtils {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Generate random bytes of specified length.
     */
    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
    
    /**
     * Generate a random coin amount between 10 and 99.
     */
    public static BigInteger randomCoinAmount() {
        return BigInteger.valueOf(10 + RANDOM.nextInt(90));
    }
    
    /**
     * Create random coin data with specified number of coins.
     */
    public static TokenCoinData randomCoinData(int numCoins) {
        Map<CoinId, BigInteger> coins = new java.util.HashMap<>();
        for (int i = 0; i < numCoins; i++) {
            coins.put(new CoinId(randomBytes(32)), randomCoinAmount());
        }
        return new TokenCoinData(coins);
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Creates a token mint commitment and submits it to the client
     */
    public static Token mintTokenForUser(
            StateTransitionClient client,
            SigningService signingService,
            byte[] nonce,
            TokenId tokenId,
            TokenType tokenType,
            TokenCoinData coinData) throws Exception {

        MaskedPredicate predicate = MaskedPredicate.create(signingService, HashAlgorithm.SHA256, nonce);
        Address address = predicate.getReference(tokenType).toAddress();
        TokenState tokenState = new TokenState(predicate, null);

        MintCommitment<?> mintCommitment = MintCommitment.create(
                new MintTransactionData(
                        tokenId,
                        tokenType,
                        new TestTokenData(randomBytes(32)).getData(),
                        coinData,
                        address,
                        randomBytes(5),
                        null,
                        null
                )
        );

        SubmitCommitmentResponse response = client.submitCommitment(mintCommitment).get();
        if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
            throw new Exception("Failed to submit mint commitment: " + response.getStatus());
        }

        InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(client, mintCommitment).get();
        return new Token(tokenState, mintCommitment.toTransaction(inclusionProof));
    }

    /**
     * Transfers a token from one user to another
     */
    public static Token transferToken(
            StateTransitionClient client,
            Token sourceToken,
            SigningService fromSigningService,
            SigningService toSigningService,
            byte[] toNonce,
            Address toAddress,
            byte[] customData,
            List<Token<?>> additionalTokens) throws Exception {

        // Create data hash if custom data provided
        DataHash dataHash = null;
        if (customData != null) {
            dataHash = hashData(customData);
        }

        // Submit transfer commitment
        TransferCommitment transferCommitment = TransferCommitment.create(
                sourceToken,
                toAddress,
                randomBytes(32),
                dataHash,
                null,
                fromSigningService
        );

        SubmitCommitmentResponse response = client.submitCommitment(sourceToken, transferCommitment).get();
        if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
            throw new Exception("Failed to submit transfer commitment: " + response.getStatus());
        }

        // Wait for inclusion proof
        InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(client, transferCommitment).get();
        Transaction<TransferTransactionData> transferTransaction = transferCommitment.toTransaction(sourceToken, inclusionProof);


        //Bob steps
        // Create predicate for recipient
        MaskedPredicate toPredicate = MaskedPredicate.create(toSigningService, HashAlgorithm.SHA256, toNonce);

        // Finalize transaction
        return client.finalizeTransaction(
                sourceToken,
                new TokenState(toPredicate, customData),
                transferTransaction,
                additionalTokens != null ? additionalTokens : List.of()
        );
    }

    /**
     * Creates random coin data with specified number of coins
     */
    public static TokenCoinData createRandomCoinData(int coinCount) {
        Map<CoinId, BigInteger> coins = new java.util.HashMap<>();
        for (int i = 0; i < coinCount; i++) {
            CoinId coinId = new CoinId(randomBytes(32));
            BigInteger value = BigInteger.valueOf(SECURE_RANDOM.nextInt(1000) + 100); // Random value between 100-1099
            coins.put(coinId, value);
        }
        return new TokenCoinData(coins);
    }

    /**
     * Generates random bytes of specified length
     */
    public static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

    /**
     * Creates a hash of the provided data
     */
    public static DataHash hashData(byte[] data) {
        return new DataHasher(HashAlgorithm.SHA256).update(data).digest();
    }

    /**
     * Creates a hash of string data
     */
    public static DataHash hashData(String data) {
        return hashData(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a signing service from a user name and optional nonce
     */
    public static SigningService createSigningServiceForUser(String userName, byte[] nonce) {
        byte[] secret = userName.getBytes(StandardCharsets.UTF_8);
        return SigningService.createFromMaskedSecret(secret, nonce);
    }

    /**
     * Sets up a user with signing service and nonce in the provided maps
     */
    public static void setupUser(String userName,
                                 Map<String, SigningService> userSigningServices,
                                 Map<String, byte[]> userNonces,
                                 Map<String, byte[]> userSecret) {
        byte[] secret = userName.getBytes(StandardCharsets.UTF_8);
        byte[] nonce = generateRandomBytes(32);
        SigningService signingService = SigningService.createFromMaskedSecret(secret, nonce);

        userSigningServices.put(userName, signingService);
        userNonces.put(userName, nonce);
        userSecret.put(userName,secret);
    }

    /**
     * Validates that a token is properly owned by a signing service
     */
    public static boolean validateTokenOwnership(Token token, SigningService signingService) {
        if (!token.verify().isSuccessful()) {
            return false;
        }
        return token.getState().getUnlockPredicate().isOwner(signingService.getPublicKey());
    }

    public static RequestId createRequestId(SigningService signingService, DataHash stateHash) {
        return RequestId.createFromImprint(signingService.getPublicKey(), stateHash.getImprint());
    }

    public static Authenticator createAuthenticator(SigningService signingService, DataHash txDataHash, DataHash stateHash) {
        return Authenticator.create(signingService, txDataHash, stateHash);
    }

    /**
     * Waits for a commitment to be included and returns the inclusion proof
     */
    public static InclusionProof waitForInclusionProof(StateTransitionClient client, Commitment commitment) throws Exception {
        return InclusionProofUtils.waitInclusionProof(client, commitment).get();
    }

    /**
     * Generates a random token ID
     */
    public static TokenId generateRandomTokenId() {
        return new TokenId(randomBytes(32));
    }

    /**
     * Generates a random token type
     */
    public static TokenType generateRandomTokenType() {
        return new TokenType(randomBytes(32));
    }

    /**
     * Creates a token type from a string identifier
     */
    public static TokenType createTokenTypeFromString(String identifier) {
        return new TokenType(identifier.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates performance metrics
     */
    public static class PerformanceValidator {
        public static void validateDuration(long actualDuration, long maxDuration, String operation) {
            if (actualDuration >= maxDuration) {
                throw new AssertionError(String.format(
                        "%s took %d ms, should be less than %d ms",
                        operation, actualDuration, maxDuration));
            }
        }

        public static void validateSuccessRate(long successful, long total, double minSuccessRate, String operation) {
            double actualRate = (double) successful / total;
            if (actualRate < minSuccessRate) {
                throw new AssertionError(String.format(
                        "%s success rate %.2f%% is below required %.2f%%",
                        operation, actualRate * 100, minSuccessRate * 100));
            }
        }
    }

    /**
     * Token operation result wrapper
     */
    public static class TokenOperationResult {
        private final boolean success;
        private final String message;
        private final Token token;
        private final Exception error;

        public TokenOperationResult(boolean success, String message, Token token, Exception error) {
            this.success = success;
            this.message = message;
            this.token = token;
            this.error = error;
        }

        public static TokenOperationResult success(String message, Token token) {
            return new TokenOperationResult(true, message, token, null);
        }

        public static TokenOperationResult failure(String message, Exception error) {
            return new TokenOperationResult(false, message, null, error);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Token getToken() { return token; }
        public Exception getError() { return error; }
    }

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }


}