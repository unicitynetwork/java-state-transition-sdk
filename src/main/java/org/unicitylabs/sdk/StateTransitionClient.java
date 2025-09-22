
package org.unicitylabs.sdk;

import org.unicitylabs.sdk.api.IAggregatorClient;
import org.unicitylabs.sdk.api.InclusionProofResponse;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.predicate.PredicateEngineService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.transaction.Commitment;
import org.unicitylabs.sdk.transaction.InclusionProof;
import org.unicitylabs.sdk.transaction.InclusionProofVerificationStatus;
import org.unicitylabs.sdk.transaction.MintCommitment;
import org.unicitylabs.sdk.transaction.MintTransactionData;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferCommitment;
import org.unicitylabs.sdk.transaction.TransferTransactionData;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class StateTransitionClient {

  protected final IAggregatorClient client;

  public StateTransitionClient(IAggregatorClient client) {
    this.client = client;
  }

  public <T extends MintTransactionData<?>> CompletableFuture<SubmitCommitmentResponse> submitCommitment(
      MintCommitment<T> commitment) {
    return this.client.submitCommitment(
        commitment.getRequestId(),
        commitment.getTransactionData().calculateHash(),
        commitment.getAuthenticator()
    );
  }

  public CompletableFuture<SubmitCommitmentResponse> submitCommitment(
      Token<?> token,
      TransferCommitment commitment) {
    if (
        !PredicateEngineService.createPredicate(
            commitment.getTransactionData().getSourceState().getPredicate()
        ).isOwner(commitment.getAuthenticator().getPublicKey())
    ) {
      throw new IllegalArgumentException(
          "Ownership verification failed: Authenticator does not match source state predicate.");
    }

    return this.client.submitCommitment(commitment.getRequestId(), commitment.getTransactionData()
        .calculateHash(), commitment.getAuthenticator());
  }

  public <T extends MintTransactionData<?>> Token<T> finalizeTransaction(
      Token<T> token,
      TokenState state,
      Transaction<TransferTransactionData> transaction
  ) {
    return this.finalizeTransaction(token, state, transaction, List.of());
  }

  public <T extends MintTransactionData<?>> Token<T> finalizeTransaction(
      Token<T> token,
      TokenState state,
      Transaction<TransferTransactionData> transaction,
      List<Token<?>> nametags
  ) {
    Objects.requireNonNull(token, "Token is null");

    return token.update(state, transaction, nametags);
  }

  public CompletableFuture<InclusionProofVerificationStatus> getTokenStatus(
      Token<? extends MintTransactionData<?>> token,
      byte[] publicKey) {
    RequestId requestId = RequestId.create(publicKey, token.getState().calculateHash());
    return this.client.getInclusionProof(requestId)
        .thenApply(response -> response.getInclusionProof().verify(requestId));
  }

  public CompletableFuture<InclusionProofResponse> getInclusionProof(Commitment<?> commitment) {
    return this.client.getInclusionProof(commitment.getRequestId());
  }
}
