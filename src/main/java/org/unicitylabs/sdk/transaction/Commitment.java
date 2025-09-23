
package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.RequestId;
import java.util.Objects;
import org.unicitylabs.sdk.bft.RootTrustBase;

/**
 * Commitment representing a submitted transaction
 *
 * @param <T> the type of transaction data
 */
public abstract class Commitment<T extends TransactionData<?>> {
  private final RequestId requestId;
  private final T transactionData;
  private final Authenticator authenticator;

  public Commitment(RequestId requestId, T transactionData, Authenticator authenticator) {
    this.requestId = requestId;
    this.transactionData = transactionData;
    this.authenticator = authenticator;
  }

  /**
   * Returns the request ID associated with this commitment.
   *
   * @return request ID
   */
  public RequestId getRequestId() {
    return requestId;
  }

  /**
   * Returns the transaction data associated with this commitment.
   *
   * @return transaction data
   */
  public T getTransactionData() {
    return transactionData;
  }

  /**
   * Returns the authenticator associated with this commitment.
   *
   * @return authenticator
   */
  public Authenticator getAuthenticator() {
    return authenticator;
  }

  public Transaction<T> toTransaction(InclusionProof inclusionProof) {
    if (inclusionProof.verify(this.getRequestId()) != InclusionProofVerificationStatus.OK) {
      throw new RuntimeException("Inclusion proof verification failed.");
    }

    if (inclusionProof.getAuthenticator().isEmpty()) {
      throw new RuntimeException("Authenticator is missing from inclusion proof.");
    }

    if (!this.getTransactionData().calculateHash().equals(inclusionProof.getTransactionHash().orElse(null))) {
      throw new RuntimeException("Payload hash mismatch.");
    }

    return new Transaction<>(this.getTransactionData(), inclusionProof);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Commitment)) {
      return false;
    }
    Commitment<?> that = (Commitment<?>) o;
    return Objects.equals(this.requestId, that.requestId) && Objects.equals(
        this.transactionData, that.transactionData) && Objects.equals(this.authenticator,
        that.authenticator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.requestId, this.transactionData, authenticator);
  }

  @Override
  public String toString() {
    return String.format("Commitment{requestId=%s, transactionData=%s, authenticator=%s}",
        this.requestId, this.transactionData, this.authenticator);
  }
}
