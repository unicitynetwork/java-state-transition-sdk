
package com.unicity.sdk;

import com.unicity.sdk.api.IAggregatorClient;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.api.SubmitCommitmentResponse;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.InclusionProofVerificationStatus;
import com.unicity.sdk.transaction.MintCommitment;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferCommitment;
import com.unicity.sdk.transaction.TransferTransactionData;
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
    if (!commitment.getTransactionData().getSourceState().getUnlockPredicate()
        .isOwner(commitment.getAuthenticator().getPublicKey())) {
      throw new IllegalArgumentException(
          "Ownership verification failed: Authenticator does not match source state predicate.");
    }

    return this.client.submitCommitment(commitment.getRequestId(), commitment.getTransactionData()
        .calculateHash(token.getId(), token.getType()), commitment.getAuthenticator());
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
      List<Token<?>> nametagTokens
  ) {
    Objects.requireNonNull(token, "Token is null");

    return token.update(state, transaction, nametagTokens);
  }

  public CompletableFuture<InclusionProofVerificationStatus> getTokenStatus(
      Token<? extends MintTransactionData<?>> token,
      byte[] publicKey) {
    RequestId requestId = RequestId.create(publicKey,
        token.getState().calculateHash(token.getId(), token.getType()));
    return this.client.getInclusionProof(requestId)
        .thenApply(inclusionProof -> inclusionProof.verify(requestId));
  }

  public CompletableFuture<InclusionProof> getInclusionProof(Commitment<?> commitment) {
    return this.client.getInclusionProof(commitment.getRequestId());
  }
}
