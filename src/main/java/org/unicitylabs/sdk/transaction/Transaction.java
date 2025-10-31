
package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Objects;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;


/**
 * Token transaction.
 *
 * @param <T> transaction data
 */
public abstract class Transaction<T extends TransactionData<?>> {

  private final T data;
  private final InclusionProof inclusionProof;

  @JsonCreator
  Transaction(
      @JsonProperty("data") T data,
      @JsonProperty("inclusionProof") InclusionProof inclusionProof
  ) {
    Objects.requireNonNull(data, "Transaction data cannot be null");
    Objects.requireNonNull(inclusionProof, "Inclusion proof cannot be null");

    this.data = data;
    this.inclusionProof = inclusionProof;
  }

  /**
   * Get transaction data.
   *
   * @return transaction data
   */
  public T getData() {
    return data;
  }

  /**
   * Get transaction inclusion proof.
   *
   * @return inclusion proof
   */
  public InclusionProof getInclusionProof() {
    return inclusionProof;
  }

  /**
   * Verify if recipient data is added to transaction.
   *
   * @param stateData recipient data
   * @return true if contains given data hash
   */
  public boolean containsRecipientData(byte[] stateData) {
    if (this.data.getRecipientDataHash().isPresent() == (stateData == null)) {
      return false;
    }

    if (this.data.getRecipientDataHash().isEmpty()) {
      return true;
    }

    DataHasher hasher = new DataHasher(HashAlgorithm.SHA256);
    hasher.update(stateData);
    return hasher.digest().equals(this.data.getRecipientDataHash().orElse(null));
  }

  /**
   * Convert transaction to JSON string.
   *
   * @return JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(Transaction.class, e);
    }
  }

  /**
   * Convert transaction to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        this.data.toCbor(),
        this.inclusionProof.toCbor()
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Transaction)) {
      return false;
    }
    Transaction<?> that = (Transaction<?>) o;
    return Objects.equals(this.data, that.data) && Objects.equals(this.inclusionProof,
        that.inclusionProof);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.data, this.inclusionProof);
  }

  @Override
  public String toString() {
    return String.format("Transaction{data=%s, inclusionProof=%s}", this.data, this.inclusionProof);
  }
}
