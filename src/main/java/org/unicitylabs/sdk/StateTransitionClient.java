
package org.unicitylabs.sdk;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.unicitylabs.sdk.api.AggregatorClient;
import org.unicitylabs.sdk.api.CertificationResponse;
import org.unicitylabs.sdk.api.InclusionProofResponse;
import org.unicitylabs.sdk.api.StateId;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.PredicateEngineService;
import org.unicitylabs.sdk.signing.MintSigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.transaction.InclusionProofVerificationStatus;
import org.unicitylabs.sdk.transaction.MintCommitment;
import org.unicitylabs.sdk.transaction.MintReasonFactory;
import org.unicitylabs.sdk.transaction.MintTransactionState;
import org.unicitylabs.sdk.transaction.TransferCommitment;
import org.unicitylabs.sdk.transaction.TransferTransaction;
import org.unicitylabs.sdk.verification.VerificationException;

/**
 * Client for handling state transitions of tokens, including submitting commitments and finalizing transactions.
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
   * @return A CompletableFuture that resolves to the response from the aggregator.
   */
  public CompletableFuture<CertificationResponse> submitCommitment(MintCommitment commitment) {
    return this.client.submitCertificationRequest(commitment.getCertificationData(), false);
  }

  /**
   * Submits a transfer commitment to the aggregator after verifying ownership.
   *
   * @param commitment The transfer commitment to submit.
   * @return A CompletableFuture that resolves to the response from the aggregator.
   * @throws IllegalArgumentException if ownership verification fails.
   */
  public CompletableFuture<CertificationResponse> submitCommitment(TransferCommitment commitment) {
    if (
        !PredicateEngineService.createPredicate(
            commitment.getTransactionData().getSourceState().getPredicate()
        ).isOwner(commitment.getCertificationData().getPublicKey())
    ) {
      throw new IllegalArgumentException(
          "Ownership verification failed: Authenticator does not match source state predicate.");
    }

    return this.client.submitCertificationRequest(commitment.getCertificationData(), false);
  }

  /**
   * Finalizes a transaction by updating the token state based on the provided transaction data without nametags.
   *
   * @param trustBase         The root trust base for inclusion proof verification.
   * @param mintReasonFactory factory to create mint transaction reasons
   * @param token             The token to be updated.
   * @param state             The current state of the token.
   * @param transaction       The transaction containing transfer data.
   * @return The updated token after applying the transaction.
   * @throws VerificationException if verification fails during the update process.
   */
  public Token finalizeTransaction(
      RootTrustBase trustBase,
      MintReasonFactory mintReasonFactory,
      Token token,
      TokenState state,
      TransferTransaction transaction
  ) throws VerificationException {
    return this.finalizeTransaction(trustBase, mintReasonFactory, token, state, transaction, List.of());
  }

  /**
   * Finalizes a transaction by updating the token state based on the provided transaction data and nametags.
   *
   * @param trustBase         The root trust base for inclusion proof verification.
   * @param mintReasonFactory factory to create mint transaction reasons
   * @param token             The token to be updated.
   * @param state             The current state of the token.
   * @param transaction       The transaction containing transfer data.
   * @param nametags          A list of tokens used as nametags in the transaction.
   * @return The updated token after applying the transaction.
   * @throws VerificationException if verification fails during the update process.
   */
  public Token finalizeTransaction(
      RootTrustBase trustBase,
      MintReasonFactory mintReasonFactory,
      Token token,
      TokenState state,
      TransferTransaction transaction,
      List<Token> nametags
  ) throws VerificationException {
    Objects.requireNonNull(token, "Token is null");

    return token.update(trustBase, mintReasonFactory, state, transaction, nametags);
  }

  /**
   * Retrieves the inclusion proof for a given state id.
   *
   * @param stateId The state ID of inclusion proof to retrieve.
   * @return A CompletableFuture that resolves to the inclusion proof response from the aggregator.
   */
  public CompletableFuture<InclusionProofResponse> getInclusionProof(StateId stateId) {
    return this.client.getInclusionProof(stateId);
  }

  /**
   * Check if state is already spent for given state id.
   *
   * @param stateId   state id
   * @param trustBase root trust base
   * @return A CompletableFuture that resolves to true if state is spent, false otherwise.
   */
  public CompletableFuture<Boolean> isStateSpent(StateId stateId, RootTrustBase trustBase) {
    return this.getInclusionProof(stateId)
        .thenApply(inclusionProof -> {
          InclusionProofVerificationStatus result = inclusionProof.getInclusionProof().verify(trustBase, stateId);
          switch (result) {
            case OK:
              return true;
            case PATH_NOT_INCLUDED:
              return false;
            default:
              throw new RuntimeException(
                  String.format("Inclusion proof verification failed with status %s", result)
              );
          }
        });
  }


  /**
   * Get inclusion proof for current token state.
   *
   * @param token     token
   * @param publicKey public key
   * @param trustBase trustBase
   * @return A CompletableFuture that resolves to the inclusion proof response from the aggregator.
   */
  public CompletableFuture<Boolean> isStateSpent(
      Token token,
      byte[] publicKey,
      RootTrustBase trustBase
  ) {
    Predicate predicate = PredicateEngineService.createPredicate(token.getState().getPredicate());
    if (!predicate.isOwner(publicKey)) {
      throw new IllegalArgumentException("Given key is not owner of the token.");
    }

    return this.isStateSpent(StateId.create(publicKey, token.getState()), trustBase);
  }

  /**
   * Check if token id is already minted.
   *
   * @param tokenId   token id
   * @param trustBase root trust base
   * @return A CompletableFuture that resolves to true if token id is spent, false otherwise.
   */
  public CompletableFuture<Boolean> isMinted(TokenId tokenId, RootTrustBase trustBase) {
    return this.isStateSpent(
        StateId.create(
            MintSigningService.create(tokenId).getPublicKey(),
            MintTransactionState.create(tokenId)
        ),
        trustBase
    );
  }


}
