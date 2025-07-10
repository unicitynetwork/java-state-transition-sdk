
package com.unicity.sdk;

import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.api.AggregatorClient;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.api.SubmitCommitmentResponse;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.signing.ISigningService;
import com.unicity.sdk.shared.signing.SigningService;
import com.unicity.sdk.shared.util.HexConverter;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.transaction.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StateTransitionClient {
    public static final byte[] MINTER_SECRET = HexConverter.decode("495f414d5f554e4956455253414c5f4d494e5445525f464f525f");
    
    protected final AggregatorClient aggregatorClient;

    public StateTransitionClient(AggregatorClient aggregatorClient) {
        this.aggregatorClient = aggregatorClient;
    }

    public <T extends MintTransactionData<?>> CompletableFuture<Commitment<T>> submitMintTransaction(T transactionData) {
        return SigningService.createFromSecret(MINTER_SECRET, transactionData.getTokenId().getBytes())
                .thenCompose(signingService -> {
                    System.out.println("Minter tokenId: " + transactionData.getTokenId().toJSON());
                    System.out.println("Minter publicKey from secret: " + HexConverter.encode(signingService.getPublicKey()));
                    return sendTransaction(transactionData, signingService);
                });
    }

    public CompletableFuture<Commitment<TransactionData>> submitTransaction(
            TransactionData transactionData,
            ISigningService<?> signingService) {
        return transactionData.getSourceState().getUnlockPredicate().isOwner(signingService.getPublicKey())
                .thenCompose(isOwner -> {
                    if (!isOwner) {
                        return CompletableFuture.failedFuture(new Exception("Failed to unlock token"));
                    }
                    return sendTransaction(transactionData, signingService);
                });
    }

    public <T extends ISerializable> CompletableFuture<Transaction<T>> createTransaction(
            Commitment<T> commitment,
            InclusionProof inclusionProof) {
        return inclusionProof.verify(commitment.getRequestId())
                .thenCompose(status -> {
                    if (status != InclusionProofVerificationStatus.OK) {
                        return CompletableFuture.failedFuture(new Exception("Inclusion proof verification failed."));
                    }

                    // For mint transactions, authenticator might be null in the inclusion proof
                    // This is expected behavior from the aggregator

                    // Check transaction hash if applicable
                    T transactionData = commitment.getTransactionData();
                    DataHash txHash = null;
                    if (transactionData instanceof TransactionData) {
                        txHash = ((TransactionData) transactionData).getHash();
                    } else if (transactionData instanceof MintTransactionData) {
                        txHash = ((MintTransactionData<?>) transactionData).getHash();
                    }
                    
                    // Check transaction hash if both are present
                    if (txHash != null && inclusionProof.getTransactionHash() != null) {
                        if (!inclusionProof.getTransactionHash().equals(txHash)) {
                            return CompletableFuture.failedFuture(new Exception("Payload hash mismatch"));
                        }
                    }

                    return CompletableFuture.completedFuture(new Transaction<>(commitment.getTransactionData(), inclusionProof));
                });
    }

    public <T extends Transaction<MintTransactionData<?>>> CompletableFuture<Token<T>> finishTransaction(
            Token<T> token,
            TokenState state,
            Transaction<TransactionData> transaction) {
        return finishTransaction(token, state, transaction, new ArrayList<>());
    }

    public <T extends Transaction<MintTransactionData<?>>> CompletableFuture<Token<T>> finishTransaction(
            Token<T> token,
            TokenState state,
            Transaction<TransactionData> transaction,
            List<Token<?>> nametagTokens) {
        return transaction.getData().getSourceState().getUnlockPredicate().verify(transaction)
                .thenCompose(verified -> {
                    if (!verified) {
                        return CompletableFuture.failedFuture(new Exception("Predicate verification failed"));
                    }

                    return DirectAddress.create(state.getUnlockPredicate().getReference())
                            .thenCompose(expectedAddress -> {
                                if (!expectedAddress.toString().equals(transaction.getData().getRecipient())) {
                                    return CompletableFuture.failedFuture(new Exception("Recipient address mismatch"));
                                }

                                List<Transaction<?>> transactions = new ArrayList<>(token.getTransactions());
                                transactions.add(transaction);

                                return transaction.containsData(state.getData())
                                        .thenCompose(contains -> {
                                            if (!contains) {
                                                return CompletableFuture.failedFuture(new Exception("State data is not part of transaction."));
                                            }

                                            return CompletableFuture.completedFuture(
                                                    new Token<>(state, token.getGenesis(), transactions, nametagTokens));
                                        });
                            });
                });
    }

    public CompletableFuture<InclusionProofVerificationStatus> getTokenStatus(
            Token<? extends Transaction<MintTransactionData<?>>> token,
            byte[] publicKey) {
        return RequestId.create(publicKey, token.getState().getHash())
                .thenCompose(requestId -> aggregatorClient.getInclusionProof(requestId))
                .thenCompose(inclusionProof -> 
                        RequestId.create(publicKey, token.getState().getHash())
                                .thenCompose(inclusionProof::verify));
    }

    public CompletableFuture<InclusionProof> getInclusionProof(Commitment<?> commitment) {
        return aggregatorClient.getInclusionProof(commitment.getRequestId());
    }

    private <TD extends ISerializable> CompletableFuture<Commitment<TD>> sendTransaction(
            TD transactionData,
            ISigningService<?> signingService) {
        
        // Get the source state hash and request ID based on the transaction data type
        CompletableFuture<RequestId> requestIdFuture;
        DataHash sourceStateHash;
        DataHash transactionHash;
        
        if (transactionData instanceof TransactionData) {
            TransactionData txData = (TransactionData) transactionData;
            sourceStateHash = txData.getSourceState().getHash();
            transactionHash = txData.getHash();
            requestIdFuture = RequestId.create(signingService.getPublicKey(), sourceStateHash);
        } else if (transactionData instanceof MintTransactionData) {
            MintTransactionData<?> mintData = (MintTransactionData<?>) transactionData;
            // For mint transactions, use the sourceState from the mint data
            RequestId mintSourceState = mintData.getSourceState();
            sourceStateHash = mintSourceState.getHash();
            transactionHash = mintData.getHash();
            // TypeScript creates RequestId using: RequestId.create(signingService.publicKey, transactionData.sourceState.hash)
            requestIdFuture = RequestId.create(signingService.getPublicKey(), mintSourceState.getHash());
        } else {
            return CompletableFuture.failedFuture(new Exception("Unsupported transaction data type"));
        }
        
        return requestIdFuture.thenCompose(requestId -> 
                    Authenticator.create(
                            signingService,
                            transactionHash,
                            sourceStateHash)
                            .thenCompose(authenticator -> {
                                // Debug logging
                                System.out.println("Authenticator JSON: " + authenticator.toJSON());
                                System.out.println("RequestId: " + requestId.toJSON());
                                System.out.println("TransactionHash: " + transactionHash.toJSON());
                                if (transactionData instanceof MintTransactionData) {
                                    MintTransactionData<?> mintData = (MintTransactionData<?>) transactionData;
                                    System.out.println("MintSourceState: " + mintData.getSourceState().toJSON());
                                    System.out.println("MintSourceState hash: " + mintData.getSourceState().getHash().toJSON());
                                    System.out.println("SigningService publicKey: " + HexConverter.encode(signingService.getPublicKey()));
                                }
                                
                                return aggregatorClient.submitTransaction(requestId, transactionHash, authenticator)
                                        .thenCompose(result -> {
                                            if (result.getStatus() != SubmitCommitmentStatus.SUCCESS) {
                                                return CompletableFuture.failedFuture(
                                                        new Exception("Could not submit transaction: " + result.getStatus()));
                                            }
                                            return CompletableFuture.completedFuture(
                                                    new Commitment<>(requestId, transactionData, authenticator));
                                        });
                            })
                );
    }
    
    public CompletableFuture<SubmitCommitmentResponse> submitCommitment(Commitment<?> commitment) {
        DataHash txHash = null;
        ISerializable transactionData = commitment.getTransactionData();
        if (transactionData instanceof TransactionData) {
            txHash = ((TransactionData) transactionData).getHash();
        } else if (transactionData instanceof MintTransactionData) {
            txHash = ((MintTransactionData<?>) transactionData).getHash();
        }
        
        if (txHash == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Cannot get hash from transaction data"));
        }
        
        return aggregatorClient.submitTransaction(
            commitment.getRequestId(),
            txHash,
            commitment.getAuthenticator()
        );
    }
    
    public AggregatorClient getAggregatorClient() {
        return aggregatorClient;
    }
}
