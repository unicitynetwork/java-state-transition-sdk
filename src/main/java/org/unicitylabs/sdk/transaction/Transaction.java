
package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.function.Function;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import java.util.Objects;
import org.unicitylabs.sdk.predicate.EncodedPredicate;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.token.TokenState;


public abstract class Transaction<T extends TransactionData<?>> {

  private final T data;
  private final InclusionProof inclusionProof;

  @JsonCreator
  public Transaction(@JsonProperty("data") T data, @JsonProperty("inclusionProof") InclusionProof inclusionProof) {
    Objects.requireNonNull(data, "Transaction data cannot be null");
    Objects.requireNonNull(inclusionProof, "Inclusion proof cannot be null");

    this.data = data;
    this.inclusionProof = inclusionProof;
  }

  public T getData() {
    return data;
  }

  public InclusionProof getInclusionProof() {
    return inclusionProof;
  }

  public boolean containsRecipientDataHash(byte[] stateData) {
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

  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(Transaction.class, e);
    }
  }

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
