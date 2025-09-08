package org.unicitylabs.sdk.mtree.plain;

import org.unicitylabs.sdk.hash.HashAlgorithm;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

class PendingLeafBranch implements LeafBranch {

  private final BigInteger path;
  private final byte[] value;

  public PendingLeafBranch(BigInteger path, byte[] value) {
    this.path = path;
    this.value = value;
  }

  public BigInteger getPath() {
    return this.path;
  }

  public byte[] getValue() {
    return Arrays.copyOf(this.value, this.value.length);
  }

  public FinalizedLeafBranch finalize(HashAlgorithm hashAlgorithm) {
    return FinalizedLeafBranch.create(this.path, this.value, hashAlgorithm);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PendingLeafBranch)) {
      return false;
    }
    PendingLeafBranch that = (PendingLeafBranch) o;
    return Objects.equals(this.path, that.path) && Objects.deepEquals(this.value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, Arrays.hashCode(this.value));
  }
}
