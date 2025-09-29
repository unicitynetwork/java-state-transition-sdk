
package org.unicitylabs.sdk.transaction;

import java.util.Objects;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.RequestId;

/**
 * Commitment representing a submitted transaction.
 *
 * @param <T> the type of transaction data
 */
public abstract class Commitment<T extends TransactionData<?>> {

  private final RequestId requestId;
  private final T transactionData;
  private final Authenticator authenticator;

  /**
   * Create commitment.
   *
   * @param requestId       request id
   * @param transactionData transaction data
   * @param authenticator   authenticator
   */
  protected Commitment(RequestId requestId, T transactionData, Authenticator authenticator) {
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

  /**
   * Convert commitment to transaction.
   *
   * @param inclusionProof Commitment inclusion proof
   * @return transaction
   */
  public abstract Transaction<T> toTransaction(InclusionProof inclusionProof);

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Commitment)) {
      return false;
    }
    Commitment<?> that = (Commitment<?>) o;
    return Objects.equals(this.requestId, that.requestId)
        && Objects.equals(this.transactionData, that.transactionData)
        && Objects.equals(this.authenticator, that.authenticator);
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
