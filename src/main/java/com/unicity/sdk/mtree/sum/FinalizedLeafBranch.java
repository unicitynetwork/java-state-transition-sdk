package com.unicity.sdk.mtree.sum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTree.LeafValue;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.serializer.cbor.CborSerializationException;
import com.unicity.sdk.util.BigIntegerConverter;
import java.math.BigInteger;
import java.util.Objects;

class FinalizedLeafBranch implements LeafBranch, FinalizedBranch {

  private final BigInteger path;
  private final LeafValue value;
  private final DataHash hash;

  private FinalizedLeafBranch(BigInteger path, LeafValue value, DataHash hash) {
    this.path = path;
    this.value = value;
    this.hash = hash;
  }

  public static FinalizedLeafBranch create(BigInteger path, LeafValue value,
      HashAlgorithm hashAlgorithm) {
    try {
      DataHash hash = new DataHasher(hashAlgorithm)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(
              UnicityObjectMapper.CBOR.createArrayNode()
                  .add(BigIntegerConverter.encode(path))
                  .add(value.getValue())
                  .add(BigIntegerConverter.encode(value.getCounter()))
          ))
          .digest();
      return new FinalizedLeafBranch(path, value, hash);
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
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
