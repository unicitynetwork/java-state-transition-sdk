package org.unicitylabs.sdk.bft;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.unicitylabs.sdk.util.HexConverter;

public class UnicityTreeCertificate {

  private final BigInteger version;
  private final BigInteger partitionIdentifier;
  private final List<HashStep> steps;

  public UnicityTreeCertificate(
      BigInteger version,
      BigInteger partitionIdentifier,
      List<HashStep> steps
  ) {
    Objects.requireNonNull(version, "Version cannot be null");
    Objects.requireNonNull(partitionIdentifier, "Partition identifier cannot be null");
    Objects.requireNonNull(steps, "Steps cannot be null");

    this.version = version;
    this.partitionIdentifier = partitionIdentifier;
    this.steps = steps;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UnicityTreeCertificate)) {
      return false;
    }
    UnicityTreeCertificate that = (UnicityTreeCertificate) o;
    return Objects.equals(this.version, that.version) && Objects.equals(
        this.partitionIdentifier, that.partitionIdentifier) && Objects.equals(this.steps,
        that.steps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.version, this.partitionIdentifier, this.steps);
  }

  @Override
  public String toString() {
    return String.format("UnicityTreeCertificate{version=%s, partitionIdentifier=%s, steps=%s",
        this.version, this.partitionIdentifier, this.steps);
  }

  public static class HashStep {

    private final BigInteger key;
    private final byte[] hash;

    public HashStep(BigInteger key, byte[] hash) {
      Objects.requireNonNull(key, "Key cannot be null");
      Objects.requireNonNull(hash, "Hash cannot be null");

      this.key = key;
      this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof HashStep)) {
        return false;
      }
      HashStep hashStep = (HashStep) o;
      return Objects.equals(this.key, hashStep.key) && Objects.deepEquals(this.hash,
          hashStep.hash);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.key, Arrays.hashCode(this.hash));
    }

    @Override
    public String toString() {
      return String.format("UnicityTreeCertificate.HashStep{key=%s, hash=%s",
          this.key, HexConverter.encode(this.hash));
    }
  }
}
