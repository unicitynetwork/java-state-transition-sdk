package com.unicity.sdk.mtree.plain;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.util.BigIntegerConverter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

class FinalizedLeafBranch implements LeafBranch, FinalizedBranch {

  private final BigInteger path;
  private final byte[] value;
  private final DataHash hash;

  private FinalizedLeafBranch(BigInteger path, byte[] value, DataHash hash) {
    this.path = path;
    this.value = Arrays.copyOf(value, value.length);
    this.hash = hash;
  }

  public static FinalizedLeafBranch create(BigInteger path, byte[] value,
      HashAlgorithm hashAlgorithm) {
    DataHash hash = new DataHasher(hashAlgorithm)
        .update(BigIntegerConverter.encode(path))
        .update(value)
        .digest();
    return new FinalizedLeafBranch(path, value, hash);
  }

  public BigInteger getPath() {
    return this.path;
  }

  public byte[] getValue() {
    return Arrays.copyOf(this.value, this.value.length);
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
    return Objects.equals(this.path, that.path) && Arrays.equals(this.value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, Arrays.hashCode(this.value));
  }
}
