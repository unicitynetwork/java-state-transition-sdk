
package com.unicity.sdk.transaction;

import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.util.HexConverter;
import java.util.Objects;


public class Transaction<T extends TransactionData<?>> {

  private final T data;
  private final InclusionProof inclusionProof;

  public Transaction(T data, InclusionProof inclusionProof) {
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
    if (!(data instanceof TransferTransactionData)) {
      return false;
    }

    TransferTransactionData txData = (TransferTransactionData) data;

    // If transaction has no data hash and state data is empty, they match
    if (txData.getDataHash() == null && (stateData == null || stateData.length == 0)) {
      return false;
    }

    // If one is null but not the other, they don't match
    if (txData.getDataHash() == null || stateData == null) {
      return false;
    }

    DataHasher hasher = new DataHasher(HashAlgorithm.SHA256);
    hasher.update(stateData);

    return hasher.digest().equals(txData.getDataHash());
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
