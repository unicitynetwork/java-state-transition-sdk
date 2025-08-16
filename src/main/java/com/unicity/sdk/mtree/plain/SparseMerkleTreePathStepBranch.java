package com.unicity.sdk.mtree.plain;

import com.unicity.sdk.util.HexConverter;
import java.util.Arrays;

public class SparseMerkleTreePathStepBranch {

  private final byte[] value;

  public SparseMerkleTreePathStepBranch(byte[] value) {
    this.value = value;
  }

  public byte[] getValue() {
    return this.value != null ? Arrays.copyOf(this.value, this.value.length) : null;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SparseMerkleTreePathStepBranch)) {
      return false;
    }
    SparseMerkleTreePathStepBranch that = (SparseMerkleTreePathStepBranch) o;
    return Arrays.equals(this.value, that.value);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.value);
  }

  @Override
  public String toString() {
    return String.format("MerkleTreePathStepBranch{value=%s}",
        this.value != null ? HexConverter.encode(this.value) : null);
  }
}
