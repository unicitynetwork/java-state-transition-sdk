
package org.unicitylabs.sdk;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.unicitylabs.sdk.api.AggregatorClient;
import org.unicitylabs.sdk.api.InclusionProofResponse;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.predicate.PredicateEngineService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.transaction.Commitment;
import org.unicitylabs.sdk.transaction.InclusionProofVerificationStatus;
import org.unicitylabs.sdk.transaction.MintCommitment;
import org.unicitylabs.sdk.transaction.MintTransactionReason;
import org.unicitylabs.sdk.transaction.TransferCommitment;
import org.unicitylabs.sdk.transaction.TransferTransaction;
import org.unicitylabs.sdk.verification.VerificationException;

/**
 * Client for handling state transitions of tokens, including submitting commitments and finalizing
 * transactions.
 */
public class StateTransitionClient {

  /**
   * The aggregator client used for submitting commitments and retrieving inclusion proofs.
   */
  protected final AggregatorClient client;

  /**
   * Creates a new StateTransitionClient with the specified aggregator client.
   *
   * @param client The aggregator client to use for communication.
   */
  public StateTransitionClient(AggregatorClient client) {
    this.client = client;
  }

  /**
   * Submits a mint commitment to the aggregator.
   *
   * @param commitment The mint commitment to submit.
   * @param <R>        The type of mint transaction data.
   * @return A CompletableFuture that resolves to the response from the aggregator.
   */
  public <R extends MintTransactionReason>
        CompletableFuture<SubmitCommitmentResponse> submitCommitment(MintCommitment<R> commitment) {
    return this.client.submitCommitment(
        commitment.getRequestId(),
        commitment.getTransactionData().calculateHash(),
        commitment.getAuthenticator()
    );
  }

  /**
   * Submits a transfer commitment to the aggregator after verifying ownership.
   *
   * @param commitment The transfer commitment to submit.
   * @return A CompletableFuture that resolves to the response from the aggregator.
   * @throws IllegalArgumentException if ownership verification fails.
   */
  public CompletableFuture<SubmitCommitmentResponse> submitCommitment(
      TransferCommitment commitment
  ) {
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

  /**
   * Finalizes a transaction by updating the token state based on the provided transaction data
   * without nametags.
   *
   * @param trustBase   The root trust base for inclusion proof verification.
   * @param token       The token to be updated.
   * @param state       The current state of the token.
   * @param transaction The transaction containing transfer data.
   * @param <R>         The type of mint transaction data.
   * @return The updated token after applying the transaction.
   * @throws VerificationException if verification fails during the update process.
   */
  public <R extends MintTransactionReason> Token<R> finalizeTransaction(
      RootTrustBase trustBase,
      Token<R> token,
      TokenState state,
      TransferTransaction transaction
  ) throws VerificationException {
    return this.finalizeTransaction(trustBase, token, state, transaction, Collections.emptyList());
  }

  /**
   * Finalizes a transaction by updating the token state based on the provided transaction data and
   * nametags.
   *
   * @param trustBase   The root trust base for inclusion proof verification.
   * @param token       The token to be updated.
   * @param state       The current state of the token.
   * @param transaction The transaction containing transfer data.
   * @param nametags    A list of tokens used as nametags in the transaction.
   * @param <R>         The type of mint transaction data of token.
   * @return The updated token after applying the transaction.
   * @throws VerificationException if verification fails during the update process.
   */
  public <R extends MintTransactionReason> Token<R> finalizeTransaction(
      RootTrustBase trustBase,
      Token<R> token,
      TokenState state,
      TransferTransaction transaction,
      List<Token<?>> nametags
  ) throws VerificationException {
    Objects.requireNonNull(token, "Token is null");

    return token.update(trustBase, state, transaction, nametags);
  }

  /**
   * Retrieves the inclusion proof for a token and verifies its status against the provided public
   * key and trust base.
   *
   * @param token     The token for which to retrieve the inclusion proof.
   * @param publicKey The public key associated with the token.
   * @param trustBase The root trust base for verification.
   * @return A CompletableFuture that resolves to the inclusion proof verification status.
   */
  public CompletableFuture<InclusionProofVerificationStatus> getTokenStatus(
      Token<?> token,
      byte[] publicKey,
      RootTrustBase trustBase
  ) {
    RequestId requestId = RequestId.create(publicKey, token.getState().calculateHash());
    return this.client.getInclusionProof(requestId)
        .thenApply(response -> response.getInclusionProof().verify(requestId, trustBase));
  }

  /**
   * Retrieves the inclusion proof for a given commitment.
   *
   * @param commitment The commitment for which to retrieve the inclusion proof.
   * @return A CompletableFuture that resolves to the inclusion proof response from the aggregator.
   */
  public CompletableFuture<InclusionProofResponse> getInclusionProof(Commitment<?> commitment) {
    return this.client.getInclusionProof(commitment.getRequestId());
  }
}
