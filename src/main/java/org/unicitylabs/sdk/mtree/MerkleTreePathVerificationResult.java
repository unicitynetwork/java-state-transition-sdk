package org.unicitylabs.sdk.mtree;

import java.util.Objects;

public class MerkleTreePathVerificationResult {

  private final boolean pathValid;
  private final boolean pathIncluded;

  public MerkleTreePathVerificationResult(boolean pathValid, boolean pathIncluded) {
    this.pathValid = pathValid;
    this.pathIncluded = pathIncluded;
  }

  public boolean isPathValid() {
    return pathValid;
  }

  public boolean isPathIncluded() {
    return pathIncluded;
  }

  public boolean isValid() {
    return pathValid && pathIncluded;
  }

  @Override
  public String toString() {
    return String.format("MerkleTreePathVerificationResult{pathValid=%b, pathIncluded=%b}",
        pathValid, pathIncluded);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MerkleTreePathVerificationResult)) {
      return false;
    }
    MerkleTreePathVerificationResult that = (MerkleTreePathVerificationResult) o;
    return pathValid == that.pathValid && pathIncluded == that.pathIncluded;
  }

  @Override
  public int hashCode() {
    return Objects.hash(pathValid, pathIncluded);
  }
}
