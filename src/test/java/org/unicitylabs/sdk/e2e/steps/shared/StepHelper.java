package com.unicity.sdk.e2e.steps.shared;

import com.fasterxml.jackson.core.type.TypeReference;
import com.unicity.sdk.address.Address;
import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.address.ProxyAddress;
import com.unicity.sdk.api.SubmitCommitmentResponse;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import com.unicity.sdk.e2e.config.CucumberConfiguration;
import com.unicity.sdk.e2e.context.TestContext;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.*;
import com.unicity.sdk.util.InclusionProofUtils;
import com.unicity.sdk.utils.TestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.unicity.sdk.utils.TestUtils.randomBytes;


public class StepHelper {

    private final TestContext context;

    public StepHelper() {  // ✅ Public zero-argument constructor
        this.context = CucumberConfiguration.getTestContext();
    }

    public Token createNameTagTokenForUser(String userName, TokenType type, String nametag, String nametagData) throws Exception {
        SigningService signingService = context.getUserSigningServices().get(userName);
        byte[] nametagNonce = TestUtils.generateRandomBytes(32);

        MaskedPredicate nametagPredicate = MaskedPredicate.create(
                SigningService.createFromSecret(context.getUserSecret().get(userName), nametagNonce),
                HashAlgorithm.SHA256,
                nametagNonce
        );

        TokenType nametagTokenType = TestUtils.generateRandomTokenType();
        DirectAddress nametagAddress = nametagPredicate.getReference(nametagTokenType).toAddress();

        // Get user's main address for the nametag
        byte[] userNonce = context.getUserNonces().get(userName);
        MaskedPredicate userPredicate = MaskedPredicate.create(signingService, HashAlgorithm.SHA256, userNonce);
        context.getUserPredicate().put(userName, userPredicate);

        DirectAddress userAddress = userPredicate.getReference(type).toAddress();

        var nametagMintCommitment = com.unicity.sdk.transaction.MintCommitment.create(
                new NametagMintTransactionData<>(
                        nametag,
                        nametagTokenType,
                        nametagData.getBytes(StandardCharsets.UTF_8),
                        null,
                        nametagAddress,
                        TestUtils.generateRandomBytes(32),
                        userAddress
                )
        );

        SubmitCommitmentResponse response = context.getClient().submitCommitment(nametagMintCommitment).get();
        if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
            throw new Exception("Failed to submit nametag mint commitment: " + response.getStatus());
        }

        InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(context.getClient(), nametagMintCommitment).get();
        Transaction<? extends MintTransactionData<?>> nametagGenesis = nametagMintCommitment.toTransaction(inclusionProof);

        return new Token(
                new com.unicity.sdk.token.NameTagTokenState(nametagPredicate, userAddress),
                nametagGenesis
        );
    }

    public void transferToken(String fromUser, String toUser, Token token, Address toAddress, String customData) throws Exception {
        SigningService fromSigningService = context.getUserSigningServices().get(fromUser);

        // Create data hash and state data if custom data provided
        DataHash dataHash = null;
        byte[] stateData = null;
        if (customData != null && !customData.isEmpty()) {
            stateData = customData.getBytes(StandardCharsets.UTF_8);
            dataHash = TestUtils.hashData(stateData);
        }

        // Submit transfer commitment
        TransferCommitment transferCommitment = TransferCommitment.create(
                token,
                toAddress,
                randomBytes(32),
                dataHash,
                null,
                fromSigningService
        );

        SubmitCommitmentResponse response = context.getClient().submitCommitment(token, transferCommitment).get();
        if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
            throw new Exception("Failed to submit transfer commitment: " + response.getStatus());
        }

        // Wait for inclusion proof
        InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(
                context.getClient(),
                transferCommitment
        ).get();
        Transaction<TransferTransactionData> transferTransaction = transferCommitment.toTransaction(
                token,
                inclusionProof
        );

        String txJson = UnicityObjectMapper.JSON.writerWithDefaultPrettyPrinter().writeValueAsString(transferTransaction);
        System.out.println(txJson);

        Transaction<TransferTransactionData> txOnReceipient = UnicityObjectMapper.JSON.readValue(
                txJson,
                new TypeReference<Transaction<TransferTransactionData>>() {}
        );


        // Finalize transaction with custom data in the token state
        List<Token<?>> additionalTokens = new ArrayList<>();
        Token nameTagToken = context.getNameTagToken(toUser);
        if (nameTagToken != null) {
            additionalTokens.add(nameTagToken);
        }

        Token finalizedToken = context.getClient().finalizeTransaction(
                token,
                new TokenState(context.getUserPredicate().get(toUser), stateData),
                transferTransaction,
                additionalTokens
        );

        context.addUserToken(toUser, finalizedToken);
    }

    public void transferToken2(String fromUser, String toUser, Token token, Address toAddress, String customData) throws Exception {
        SigningService fromSigningService = context.getUserSigningServices().get(fromUser);

        // Create data hash and state data if custom data provided
        DataHash dataHash = null;
        byte[] stateData = null;
        if (customData != null && !customData.isEmpty()) {
            stateData = customData.getBytes(StandardCharsets.UTF_8);
            dataHash = TestUtils.hashData(stateData);
        }

        // Submit transfer commitment
        TransferCommitment transferCommitment = TransferCommitment.create(
                token,
                toAddress,
                randomBytes(32),
                dataHash,
                null,
                fromSigningService
        );

        SubmitCommitmentResponse response = context.getClient().submitCommitment(token, transferCommitment).get();
        if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
            throw new Exception("Failed to submit transfer commitment: " + response.getStatus());
        }

        // Wait for inclusion proof
        InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(
                context.getClient(),
                transferCommitment
        ).get();
        Transaction<TransferTransactionData> transferTransaction = transferCommitment.toTransaction(
                token,
                inclusionProof
        );

        String txJson = UnicityObjectMapper.JSON.writerWithDefaultPrettyPrinter().writeValueAsString(transferTransaction);
        System.out.println(txJson);




        Transaction<TransferTransactionData> txOnReceipient = UnicityObjectMapper.JSON.readValue(
                txJson,
                new TypeReference<Transaction<TransferTransactionData>>() {}
        );







        byte[] nonce = randomBytes(32);
        TokenState state = new TokenState(
                MaskedPredicate.create(
                        SigningService.createFromSecret(context.getUserSecret().get(toUser), nonce),
                        HashAlgorithm.SHA256,
                        nonce
                ),
                null
        );
        Address address = state.getUnlockPredicate()
                .getReference(
                        token.getType()
                )
                .toAddress();

        byte[] nametagNonce = randomBytes(32);
        TokenState nametagTokenState = new TokenState(
                MaskedPredicate.create(
                        SigningService.createFromSecret(context.getUserSecret().get(toUser), nametagNonce),
                        HashAlgorithm.SHA256,
                        nametagNonce
                ),
                address.getAddress().getBytes(StandardCharsets.UTF_8)
        );


        Token currentNameTagToken = context.getNameTagToken(toUser);
        List<Token> nametagTokens = context.getNameTagTokens().get(toUser);
        for (int i = 0; i < nametagTokens.size(); i++) {
            String actualNametagAddress = txOnReceipient.getData().getRecipient().getAddress();
            String expectedProxyAddress = ProxyAddress.create(nametagTokens.get(i).getId()).getAddress();

            if(actualNametagAddress.equalsIgnoreCase(expectedProxyAddress)){
                currentNameTagToken = nametagTokens.get(i);
            }
        }

        TransferCommitment nametagCommitment = TransferCommitment.create(
                currentNameTagToken,
                nametagTokenState.getUnlockPredicate().getReference(currentNameTagToken.getType()).toAddress(),
                randomBytes(32),
                new DataHasher(HashAlgorithm.SHA256)
                        .update(address.getAddress().getBytes(StandardCharsets.UTF_8))
                        .digest(),
                null,
                SigningService.createFromSecret(
                        context.getUserSecret().get(toUser),
                        currentNameTagToken.getState().getUnlockPredicate().getNonce()
                )
        );

        SubmitCommitmentResponse nametagTransferResponse = context.getClient()
                .submitCommitment(currentNameTagToken, nametagCommitment)
                .get();
        if (nametagTransferResponse.getStatus() != SubmitCommitmentStatus.SUCCESS) {
            throw new Exception(String.format("Failed to submit nametag transfer commitment: %s",
                    response.getStatus()));
        }

        currentNameTagToken = context.getClient().finalizeTransaction(
                currentNameTagToken,
                nametagTokenState,
                nametagCommitment.toTransaction(
                        currentNameTagToken,
                        InclusionProofUtils.waitInclusionProof(context.getClient(), nametagCommitment).get()
                )
        );


        // Finalize transaction with custom data in the token state
        List<Token<?>> additionalTokens = new ArrayList<>();
        Token nameTagToken = currentNameTagToken;
        if (nameTagToken != null) {
            additionalTokens.add(nameTagToken);
        }

        TokenState recipientState = new TokenState(
                MaskedPredicate.create(
                        SigningService.createFromSecret(context.getUserSecret().get(toUser), nonce),
                        HashAlgorithm.SHA256,
                        nonce
                ),
                stateData
        );

        Token finalizedToken = context.getClient().finalizeTransaction(
                token,
                recipientState,
                transferTransaction,
                additionalTokens
        );

        context.addUserToken(toUser, finalizedToken);
    }

    public void transferToken3(String fromUser, String toUser, Token token, Address toAddress, String customData) throws Exception {
        SigningService fromSigningService = context.getUserSigningServices().get(fromUser);

        // Create data hash and state data if custom data provided
        DataHash dataHash = null;
        byte[] stateData = null;
        if (customData != null && !customData.isEmpty()) {
            stateData = customData.getBytes(StandardCharsets.UTF_8);
            dataHash = TestUtils.hashData(stateData);
        }

        // Submit transfer commitment
        TransferCommitment transferCommitment = TransferCommitment.create(
                token,
                toAddress,
                randomBytes(32),
                dataHash,
                null,
                fromSigningService
        );

        SubmitCommitmentResponse response = context.getClient().submitCommitment(token, transferCommitment).get();
        if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
            throw new Exception("Failed to submit transfer commitment: " + response.getStatus());
        }

        // Wait for inclusion proof
        InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(
                context.getClient(),
                transferCommitment
        ).get();
        Transaction<TransferTransactionData> transferTransaction = transferCommitment.toTransaction(
                token,
                inclusionProof
        );

        context.savePendingTransfer(toUser, token, transferTransaction);
    }

    public void finalizeTransfer(String username, Token token, Transaction<TransferTransactionData> tx) throws Exception {

        byte[] secret = context.getUserSecret().get(username);

        byte[] nonce = randomBytes(32);
        TokenState state = new TokenState(
                MaskedPredicate.create(
                        SigningService.createFromSecret(secret, nonce),
                        HashAlgorithm.SHA256,
                        nonce
                ),
                null
        );
        Address address = state.getUnlockPredicate()
                .getReference(
                        token.getType()
                )
                .toAddress();

        byte[] nametagNonce = randomBytes(32);
        TokenState nametagTokenState = new TokenState(
                MaskedPredicate.create(
                        SigningService.createFromSecret(secret, nametagNonce),
                        HashAlgorithm.SHA256,
                        nametagNonce
                ),
                address.getAddress().getBytes(StandardCharsets.UTF_8)
        );

        Token currentNameTagToken = context.getNameTagToken(username);
        List<Token> nametagTokens = context.getNameTagTokens().get(username);
        for (int i = 0; i < nametagTokens.size(); i++) {
            String actualNametagAddress = tx.getData().getRecipient().getAddress();
            String expectedProxyAddress = ProxyAddress.create(nametagTokens.get(i).getId()).getAddress();

            if(actualNametagAddress.equalsIgnoreCase(expectedProxyAddress)){
                currentNameTagToken = nametagTokens.get(i);
            }
        }

        TransferCommitment nametagCommitment = TransferCommitment.create(
                currentNameTagToken,
                nametagTokenState.getUnlockPredicate().getReference(currentNameTagToken.getType()).toAddress(),
                randomBytes(32),
                new DataHasher(HashAlgorithm.SHA256)
                        .update(address.getAddress().getBytes(StandardCharsets.UTF_8))
                        .digest(),
                null,
                SigningService.createFromSecret(
                        context.getUserSecret().get(username),
                        currentNameTagToken.getState().getUnlockPredicate().getNonce()
                )
        );

        SubmitCommitmentResponse nametagTransferResponse = context.getClient()
                .submitCommitment(currentNameTagToken, nametagCommitment)
                .get();
        if (nametagTransferResponse.getStatus() != SubmitCommitmentStatus.SUCCESS) {
            throw new Exception(String.format("Failed to submit nametag transfer commitment: %s",
                    nametagTransferResponse.getStatus()));
        }

        currentNameTagToken = context.getClient().finalizeTransaction(
                currentNameTagToken,
                nametagTokenState,
                nametagCommitment.toTransaction(
                        currentNameTagToken,
                        InclusionProofUtils.waitInclusionProof(context.getClient(), nametagCommitment).get()
                )
        );

        // Finalize transaction with custom data in the token state
        List<Token<?>> additionalTokens = new ArrayList<>();
        additionalTokens.add(currentNameTagToken);

        TokenState recipientState = new TokenState(
                MaskedPredicate.create(
                        SigningService.createFromSecret(context.getUserSecret().get(username), nonce),
                        HashAlgorithm.SHA256,
                        nonce
                ),
                null
        );

        Token finalizedToken = context.getClient().finalizeTransaction(
                token,
                recipientState,
                tx,
                additionalTokens
        );

        context.addUserToken(username, finalizedToken);
    }

    public boolean submitSingleCommitment() {
        try {
            byte[] randomSecret = TestUtils.generateRandomBytes(32);
            byte[] stateBytes = TestUtils.generateRandomBytes(32);
            byte[] txData = TestUtils.generateRandomBytes(32);

            DataHash stateHash = TestUtils.hashData(stateBytes);
            DataHash txDataHash = TestUtils.hashData(txData);
            SigningService signingService = SigningService.createFromSecret(randomSecret, null);
            var requestId = TestUtils.createRequestId(signingService, stateHash);
            var authenticator = TestUtils.createAuthenticator(signingService, txDataHash, stateHash);

            SubmitCommitmentResponse response = context.getAggregatorClient()
                    .submitCommitment(requestId, txDataHash, authenticator).get();
            return response.getStatus() == SubmitCommitmentStatus.SUCCESS;
        } catch (Exception e) {
            return false;
        }
    }
}
