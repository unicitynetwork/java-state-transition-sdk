
package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import java.util.Objects;


public class Transaction<T extends TransactionData<?>> {

  private final T data;
  private final InclusionProof inclusionProof;

  public Transaction(T data, InclusionProof inclusionProof) {
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

  public boolean containsData(byte[] stateData) {
    if (this.data.getDataHash().isPresent() == (stateData == null)) {
      return false;
    }

    if (this.data.getDataHash().isEmpty()) {
      return true;
    }

    DataHasher hasher = new DataHasher(HashAlgorithm.SHA256);
    hasher.update(stateData);
    return hasher.digest().equals(this.data.getDataHash().orElse(null));
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
