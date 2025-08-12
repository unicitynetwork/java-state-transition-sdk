package com.unicity.sdk.mtree.sum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.serializer.cbor.CborSerializationException;
import com.unicity.sdk.util.BigIntegerConverter;
import java.math.BigInteger;
import java.util.Objects;

class FinalizedNodeBranch implements NodeBranch, FinalizedBranch {

  private final BigInteger path;
  private final FinalizedBranch left;
  private final FinalizedBranch right;
  private final BigInteger counter;
  private final DataHash hash;
  private final DataHash childrenHash;

  private FinalizedNodeBranch(BigInteger path, FinalizedBranch left, FinalizedBranch right, BigInteger counter,
      DataHash childrenHash, DataHash hash) {
    this.path = path;
    this.left = left;
    this.right = right;
    this.counter = counter;
    this.childrenHash = childrenHash;
    this.hash = hash;
  }

  public static FinalizedNodeBranch create(BigInteger path, FinalizedBranch left, FinalizedBranch right,
      HashAlgorithm hashAlgorithm) {
    try {
      DataHash childrenHash = new DataHasher(hashAlgorithm)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(
                  UnicityObjectMapper.CBOR.createArrayNode()
                      .add(
                          UnicityObjectMapper.CBOR.createArrayNode()
                              .add(left.getHash().getImprint())
                              .add(left.getCounter())
                      )
                      .add(
                          UnicityObjectMapper.CBOR.createArrayNode()
                              .add(right.getHash().getImprint())
                              .add(right.getCounter())
                      )
              )
          )
          .digest();

      DataHash hash = new DataHasher(hashAlgorithm)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(
              UnicityObjectMapper.CBOR.createArrayNode()
                  .add(BigIntegerConverter.encode(path))
                  .add(childrenHash.getData())
          ))
          .digest();

      return new FinalizedNodeBranch(path, left, right, left.getCounter().add(right.getCounter()), childrenHash, hash);
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
  }

  public BigInteger getPath() {
    return this.path;
  }

  public FinalizedBranch getLeft() {
    return this.left;
  }

  public FinalizedBranch getRight() {
    return this.right;
  }

  public BigInteger getCounter() {
    return this.counter;
  }

  public DataHash getChildrenHash() {
    return this.childrenHash;
  }

  public DataHash getHash() {
    return this.hash;
  }

  public FinalizedNodeBranch finalize(HashAlgorithm hashAlgorithm) {
    return this; // Already finalized
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FinalizedNodeBranch)) {
      return false;
    }
    FinalizedNodeBranch that = (FinalizedNodeBranch) o;
    return Objects.equals(this.path, that.path) && Objects.equals(this.left, that.left) && Objects.equals(this.right, that.right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, this.left, this.right);
  }
}
