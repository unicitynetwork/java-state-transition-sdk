package org.unicitylabs.sdk.mtree.sum;

import java.math.BigInteger;
import java.util.Objects;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.BigIntegerConverter;

/**
 * Finalized node branch in a sparse merkle sum tree.
 */
class FinalizedNodeBranch implements NodeBranch, FinalizedBranch {

  private final BigInteger path;
  private final FinalizedBranch left;
  private final FinalizedBranch right;
  private final BigInteger counter;
  private final DataHash hash;
  private final DataHash childrenHash;

  private FinalizedNodeBranch(BigInteger path, FinalizedBranch left, FinalizedBranch right,
      BigInteger counter, DataHash childrenHash, DataHash hash) {
    this.path = path;
    this.left = left;
    this.right = right;
    this.counter = counter;
    this.childrenHash = childrenHash;
    this.hash = hash;
  }

  /**
   * Create a finalized node branch.
   *
   * @param path          path of the branch
   * @param left          left branch
   * @param right         right branch
   * @param hashAlgorithm hash algorithm to use
   * @return finalized node branch
   */
  public static FinalizedNodeBranch create(
      BigInteger path,
      FinalizedBranch left,
      FinalizedBranch right,
      HashAlgorithm hashAlgorithm
  ) {
    DataHash childrenHash = new DataHasher(hashAlgorithm)
        .update(
            CborSerializer.encodeArray(
                CborSerializer.encodeArray(
                    CborSerializer.encodeByteString(left.getHash().getImprint()),
                    CborSerializer.encodeByteString(BigIntegerConverter.encode(left.getCounter()))
                ),
                CborSerializer.encodeArray(
                    CborSerializer.encodeByteString(right.getHash().getImprint()),
                    CborSerializer.encodeByteString(
                        BigIntegerConverter.encode(right.getCounter()))
                )
            )
        )
        .digest();

    BigInteger counter = left.getCounter().add(right.getCounter());
    DataHash hash = new DataHasher(hashAlgorithm)
        .update(
            CborSerializer.encodeArray(
                CborSerializer.encodeByteString(BigIntegerConverter.encode(path)),
                CborSerializer.encodeByteString(childrenHash.getImprint()),
                CborSerializer.encodeByteString(BigIntegerConverter.encode(counter))
            )
        )
        .digest();

    return new FinalizedNodeBranch(path, left, right, counter, childrenHash, hash);
  }

  @Override
  public BigInteger getPath() {
    return this.path;
  }

  @Override
  public FinalizedBranch getLeft() {
    return this.left;
  }

  @Override
  public FinalizedBranch getRight() {
    return this.right;
  }

  @Override
  public BigInteger getCounter() {
    return this.counter;
  }

  /**
   * Get hash of the children (left and right).
   *
   * @return children hash
   */
  public DataHash getChildrenHash() {
    return this.childrenHash;
  }

  @Override
  public DataHash getHash() {
    return this.hash;
  }

  @Override
  public FinalizedNodeBranch finalize(HashAlgorithm hashAlgorithm) {
    return this; // Already finalized
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FinalizedNodeBranch)) {
      return false;
    }
    FinalizedNodeBranch that = (FinalizedNodeBranch) o;
    return Objects.equals(this.path, that.path) && Objects.equals(this.left, that.left)
        && Objects.equals(this.right, that.right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, this.left, this.right);
  }
}
