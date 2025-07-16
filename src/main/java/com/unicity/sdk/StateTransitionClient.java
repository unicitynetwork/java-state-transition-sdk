
package com.unicity.sdk;

import com.unicity.sdk.address.IAddress;
import com.unicity.sdk.api.AggregatorClient;
import com.unicity.sdk.api.SubmitCommitmentResponse;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.util.HexConverter;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.transaction.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StateTransitionClient {

  public static final byte[] MINTER_SECRET = HexConverter.decode(
      "495f414d5f554e4956455253414c5f4d494e5445525f464f525f");

  protected final AggregatorClient client;

  public StateTransitionClient(AggregatorClient client) {
    this.client = client;
  }

  public CompletableFuture<Commitment<MintTransactionData>> submitMintTransaction(MintTransactionData transactionData) {
    SigningService signingService = SigningService.createFromSecret(MINTER_SECRET,
        transactionData.getTokenId().getBytes());

    Commitment<MintTransactionData> commitment = Commitment.create(
        transactionData, signingService);

    return this.client.submitCommitment(
        commitment.getRequestId(), commitment.getTransactionData()
            .getHash(), commitment.getAuthenticator()).thenApply(response -> {
      if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
        throw new RuntimeException(
            String.format("Could not submit transaction: %s", response.getStatus()));
      }

      return commitment;
    });
  }

  public CompletableFuture<Commitment<TransferTransactionData>> submitTransaction(
      TransferTransactionData transactionData,
      SigningService signingService) {
    if (!transactionData.getSourceState().getUnlockPredicate()
        .isOwner(signingService.getPublicKey())) {
      throw new RuntimeException("Failed to unlock token");
    }

    return sendTransaction(transactionData, signingService);
  }

  public <T extends TransactionData<?>> CompletableFuture<Transaction<T>> createTransaction(
      Commitment<T> commitment,
      InclusionProof inclusionProof) {
//        return inclusionProof.verify(commitment.getRequestId())
//                .thenCompose(status -> {
//                    if (status != InclusionProofVerificationStatus.OK) {
//                        return CompletableFuture.failedFuture(new Exception("Inclusion proof verification failed."));
//                    }
//
//                    // For mint transactions, authenticator might be null in the inclusion proof
//                    // This is expected behavior from the aggregator
//
//                    // Check transaction hash if applicable
//                    T transactionData = commitment.getTransactionData();
//                    DataHash txHash = null;
//                    if (transactionData instanceof TransactionData) {
//                        txHash = ((TransactionData) transactionData).getHash();
//                    } else if (transactionData instanceof MintTransactionData) {
//                        txHash = ((MintTransactionData<?>) transactionData).getHash();
//                    }
//
//                    // Check transaction hash if both are present
//                    if (txHash != null && inclusionProof.getTransactionHash() != null) {
//                        if (!inclusionProof.getTransactionHash().equals(txHash)) {
//                            return CompletableFuture.failedFuture(new Exception("Payload hash mismatch"));
//                        }
//                    }
//
//                    return CompletableFuture.completedFuture(new Transaction<>(commitment.getTransactionData(), inclusionProof));
//                });
    return new CompletableFuture<>();
  }

  public <T extends Transaction<MintTransactionData>> Token<T> finishTransaction(
      Token<T> token,
      TokenState state,
      Transaction<TransferTransactionData> transaction) {
    return finishTransaction(token, state, transaction, new ArrayList<>());
  }

  public <T extends Transaction<MintTransactionData>> Token<T> finishTransaction(
      Token<T> token,
      TokenState state,
      Transaction<TransferTransactionData> transaction,
      List<Token<?>> nametagTokens) {
    if (!transaction.getData().getSourceState().getUnlockPredicate().verify(transaction)) {
      throw new RuntimeException("Predicate verification failed");
    }

    IAddress expectedAddress = state.getUnlockPredicate().getReference().toAddress();
    if (!expectedAddress.toString().equals(transaction.getData().getRecipient())) {
      throw new RuntimeException("Recipient address mismatch");
    }

    List<Transaction<?>> transactions = new ArrayList<>(token.getTransactions());
    transactions.add(transaction);

    if (!transaction.containsData(state.getData())) {
      throw new RuntimeException("State data is not part of transaction.");
    }

    return new Token<>(state, token.getGenesis(), transactions, nametagTokens);
  }

  public CompletableFuture<InclusionProofVerificationStatus> getTokenStatus(
      Token<? extends Transaction<MintTransactionData>> token,
      byte[] publicKey) {
    return new CompletableFuture<>();
//        return RequestId.create(publicKey, token.getState().getHash())
//                .thenCompose(requestId -> aggregatorClient.getInclusionProof(requestId))
//                .thenCompose(inclusionProof ->
//                        RequestId.create(publicKey, token.getState().getHash())
//                                .thenCompose(inclusionProof::verify));
  }

  public CompletableFuture<InclusionProof> getInclusionProof(Commitment<?> commitment) {
    return client.getInclusionProof(commitment.getRequestId());
  }

  private <TD extends TransactionData<?>> CompletableFuture<Commitment<TD>> sendTransaction(
      TD transactionData,
      SigningService signingService) {
    throw new RuntimeException();
//        // Get the source state hash and request ID based on the transaction data type
//        CompletableFuture<RequestId> requestIdFuture;
//        DataHash sourceStateHash;
//        DataHash transactionHash;
//
//        if (transactionData instanceof TransactionData) {
//            TransactionData txData = (TransactionData) transactionData;
//            sourceStateHash = txData.getSourceState().getHash();
//            transactionHash = txData.getHash();
//            requestIdFuture = RequestId.create(signingService.getPublicKey(), sourceStateHash);
//        } else if (transactionData instanceof MintTransactionData) {
//            MintTransactionData<?> mintData = (MintTransactionData<?>) transactionData;
//            // For mint transactions, use the sourceState from the mint data
//            RequestId mintSourceState = mintData.getSourceState();
//            sourceStateHash = mintSourceState.getHash();
//            transactionHash = mintData.getHash();
//            // TypeScript creates RequestId using: RequestId.create(signingService.publicKey, transactionData.sourceState.hash)
//            requestIdFuture = RequestId.create(signingService.getPublicKey(), mintSourceState.getHash());
//        } else {
//            return CompletableFuture.failedFuture(new Exception("Unsupported transaction data type"));
//        }
//
//        return requestIdFuture.thenCompose(requestId ->
//                    Authenticator.create(
//                            signingService,
//                            transactionHash,
//                            sourceStateHash)
//                            .thenCompose(authenticator -> {
//                                // Debug logging
//                                System.out.println("Authenticator JSON: " + authenticator.toJSON());
//                                if (transactionData instanceof MintTransactionData) {
//                                    MintTransactionData<?> mintData = (MintTransactionData<?>) transactionData;
//                                    System.out.println("SigningService publicKey: " + HexConverter.encode(signingService.getPublicKey()));
//                                }
//
//                                return aggregatorClient.submitTransaction(requestId, transactionHash, authenticator)
//                                        .thenCompose(result -> {
//                                            if (result.getStatus() != SubmitCommitmentStatus.SUCCESS) {
//                                                return CompletableFuture.failedFuture(
//                                                        new Exception("Could not submit transaction: " + result.getStatus()));
//                                            }
//                                            return CompletableFuture.completedFuture(
//                                                    new Commitment<>(requestId, transactionData, authenticator));
//                                        });
//                            })
//                );
  }

  public CompletableFuture<SubmitCommitmentResponse> submitCommitment(Commitment<?> commitment) {
    DataHash txHash = null;
    TransactionData transactionData = commitment.getTransactionData();

    if (transactionData.getHash() == null) {
      return CompletableFuture.failedFuture(
          new IllegalArgumentException("Cannot get hash from transaction data"));
    }

    return client.submitCommitment(
        commitment.getRequestId(),
        txHash,
        commitment.getAuthenticator()
    );
  }

  public AggregatorClient getClient() {
    return client;
  }
}
