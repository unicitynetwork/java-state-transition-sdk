package com.unicity.sdk;

import com.unicity.sdk.api.AggregatorClient;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import com.unicity.sdk.shared.signing.SigningService;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.OfflineCommitment;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransactionData;
import com.unicity.sdk.utils.InclusionProofUtils;

import java.util.concurrent.CompletableFuture;

/**
 * High level client implementing the offline token state transition workflow.
 */
public class OfflineStateTransitionClient extends StateTransitionClient {
    
    /**
     * Create an offline state transition client.
     * @param aggregatorClient The aggregator client to use
     */
    public OfflineStateTransitionClient(AggregatorClient aggregatorClient) {
        super(aggregatorClient);
    }
    
    /**
     * Create an offline commitment for a transaction (does not post it to the aggregator).
     *
     * @param transactionData The transaction data
     * @param signingService The signing service to use for authentication
     * @return CompletableFuture containing the offline commitment
     */
    public CompletableFuture<OfflineCommitment> createOfflineCommitment(
            TransactionData transactionData,
            SigningService signingService) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check ownership
                if (!transactionData.getSourceState().getUnlockPredicate().isOwner(signingService.getPublicKey()).get()) {
                    throw new IllegalStateException("Failed to unlock token");
                }
                
                // Create request ID
                RequestId requestId = RequestId.create(
                    signingService.getPublicKey(), 
                    transactionData.getSourceState().getHash()
                ).get();
                
                // Create authenticator
                Authenticator authenticator = Authenticator.create(
                    signingService,
                    transactionData.getHash(),
                    transactionData.getSourceState().getHash()
                ).get();
                
                return new OfflineCommitment(requestId, transactionData, authenticator);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to create offline commitment", e);
            }
        });
    }
    
    /**
     * Submit an offline transaction commitment to the aggregator.
     *
     * @param offlineCommitment The offline commitment to submit
     * @return CompletableFuture containing the transaction after confirmation
     */
    public CompletableFuture<Transaction<TransactionData>> submitOfflineTransaction(OfflineCommitment offlineCommitment) {
        // Submit the commitment to the aggregator
        return aggregatorClient.submitTransaction(
            offlineCommitment.getRequestId(),
            offlineCommitment.getTransactionData().getHash(),
            offlineCommitment.getAuthenticator()
        ).thenCompose(response -> {
            if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
                throw new RuntimeException("Failed to submit offline transaction: " + response.getStatus());
            }
            
            // Create a regular commitment from the response
            Commitment<TransactionData> commitment = new Commitment<>(
                offlineCommitment.getRequestId(),
                offlineCommitment.getTransactionData(),
                offlineCommitment.getAuthenticator()
            );
            
            // Wait for inclusion proof
            return InclusionProofUtils.waitInclusionProof(this, commitment)
                .thenCompose(inclusionProof -> 
                    // Create transaction with proof
                    createTransaction(commitment, inclusionProof)
                );
        });
    }
}
