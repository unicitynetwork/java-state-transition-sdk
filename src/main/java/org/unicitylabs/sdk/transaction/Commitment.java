
package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Objects;
import org.unicitylabs.sdk.api.CertificationData;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;

/**
 * Commitment representing a submitted transaction.
 *
 * @param <T> the type of transaction data
 */
public abstract class Commitment<T extends TransactionData<?>> {

  private final T transactionData;
  private final CertificationData certificationData;

  /**
   * Create commitment.
   *
   * @param transactionData transaction data
   * @param certificationData   certification data
   */
  protected Commitment(T transactionData, CertificationData certificationData) {
    this.transactionData = transactionData;
    this.certificationData = certificationData;
  }

  /**
   * Returns the transaction data associated with this commitment.
   *
   * @return transaction data
   */
  public T getTransactionData() {
    return this.transactionData;
  }

  /**
   * Returns the authenticator associated with this commitment.
   *
   * @return authenticator
   */
  public CertificationData getCertificationData() {
    return this.certificationData;
  }

  /**
   * Convert commitment to transaction.
   *
   * @param inclusionProof Commitment inclusion proof
   * @return transaction
   */
  public abstract Transaction<T> toTransaction(InclusionProof inclusionProof);

  /**
   * Convert commitment to JSON string.
   *
   * @return JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(InclusionProof.class, e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Commitment)) {
      return false;
    }
    Commitment<?> that = (Commitment<?>) o;
    return Objects.equals(this.transactionData, that.transactionData)
        && Objects.equals(this.certificationData, that.certificationData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.transactionData, this.certificationData);
  }

  @Override
  public String toString() {
    return String.format("Commitment{transactionData=%s, certificationData=%s}",
        this.transactionData, this.certificationData);
  }
}
