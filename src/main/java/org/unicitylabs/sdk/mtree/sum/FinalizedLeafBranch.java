package org.unicitylabs.sdk.mtree.sum;

import java.math.BigInteger;
import java.util.Objects;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTree.LeafValue;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.BigIntegerConverter;

class FinalizedLeafBranch implements LeafBranch, FinalizedBranch {

  private final BigInteger path;
  private final LeafValue value;
  private final DataHash hash;

  private FinalizedLeafBranch(BigInteger path, LeafValue value, DataHash hash) {
    this.path = path;
    this.value = value;
    this.hash = hash;
  }

  public static FinalizedLeafBranch create(
      BigInteger path,
      LeafValue value,
      HashAlgorithm hashAlgorithm
  ) {
    DataHash hash = new DataHasher(hashAlgorithm)
        .update(
            CborSerializer.encodeArray(
                CborSerializer.encodeByteString(BigIntegerConverter.encode(path)),
                CborSerializer.encodeByteString(value.getValue()),
                CborSerializer.encodeByteString(BigIntegerConverter.encode(value.getCounter()))
            )
        )
        .digest();
    return new FinalizedLeafBranch(path, value, hash);
  }

  public BigInteger getPath() {
    return this.path;
  }

  public LeafValue getValue() {
    return this.value;
  }

  public BigInteger getCounter() {
    return this.value.getCounter();
  }

  public DataHash getHash() {
    return this.hash;
  }

  public FinalizedLeafBranch finalize(HashAlgorithm hashAlgorithm) {
    return this; // Already finalized
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FinalizedLeafBranch)) {
      return false;
    }
    FinalizedLeafBranch that = (FinalizedLeafBranch) o;
    return Objects.equals(this.path, that.path) && Objects.equals(this.value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, this.value);
  }
}
