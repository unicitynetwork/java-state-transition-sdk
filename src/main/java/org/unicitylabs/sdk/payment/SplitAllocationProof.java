package org.unicitylabs.sdk.payment;

import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.DataHasher;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.smt.radixsum.FinalizedBranch;
import org.unicitylabs.sdk.smt.radixsum.FinalizedLeafBranch;
import org.unicitylabs.sdk.smt.radixsum.FinalizedNodeBranch;
import org.unicitylabs.sdk.smt.radixsum.SparseMerkleSumTreeRootNode;
import org.unicitylabs.sdk.util.BigIntegerConverter;
import org.unicitylabs.sdk.util.BitString;
import org.unicitylabs.sdk.util.HexConverter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Split allocation inclusion proof for one output asset: the explicit-depth inclusion proof of a
 * radix sparse Merkle sum tree — a leaf-to-root sequence of sibling entries
 * {@code (depth, hash, sum)} with strictly decreasing depths. The asset identifier, output
 * identifier, leaf data, leaf amount and root hash are not carried; the verifier supplies them.
 * The empty proof is valid only when the allocation tree holds a single output leaf.
 */
public final class SplitAllocationProof {

  private static final int MAX_SIBLINGS = 256;
  private static final int MAX_DEPTH = 255;
  private static final BigInteger SUM_LIMIT = BigInteger.ONE.shiftLeft(256);

  private final List<Sibling> siblings;

  private SplitAllocationProof(List<Sibling> siblings) {
    this.siblings = List.copyOf(siblings);
  }

  /**
   * Get the number of sibling entries in the proof.
   *
   * @return sibling count
   */
  public int getLength() {
    return this.siblings.size();
  }

  /**
   * Build a split allocation proof for the leaf with the given key by walking the radix sum tree
   * from the root to the leaf.
   *
   * @param root root of the asset's radix sum tree
   * @param key 32-byte output token identifier
   * @return inclusion proof for the key
   * @throws IllegalArgumentException if the key is not present in the tree
   */
  public static SplitAllocationProof create(SparseMerkleSumTreeRootNode root, byte[] key) {
    Objects.requireNonNull(root, "root cannot be null");
    Objects.requireNonNull(key, "key cannot be null");
    if (key.length != 32) {
      throw new IllegalArgumentException("Key must be 32 bytes long.");
    }

    BigInteger keyPath = BitString.fromBytesReversedLSB(key).toBigInteger();
    List<Sibling> siblings = new ArrayList<>();

    boolean isRight = keyPath.testBit(0);
    FinalizedBranch sibling = isRight ? root.getLeft() : root.getRight();
    FinalizedBranch node = isRight ? root.getRight() : root.getLeft();
    if (sibling != null) {
      siblings.add(new Sibling(0, sibling.getHash(), sibling.getValue()));
    }

    while (node instanceof FinalizedNodeBranch) {
      FinalizedNodeBranch branch = (FinalizedNodeBranch) node;
      isRight = keyPath.testBit(branch.getDepth());
      sibling = isRight ? branch.getLeft() : branch.getRight();
      node = isRight ? branch.getRight() : branch.getLeft();
      if (sibling != null) {
        siblings.add(new Sibling(branch.getDepth(), sibling.getHash(), sibling.getValue()));
      }
    }

    if (!(node instanceof FinalizedLeafBranch)) {
      throw new IllegalArgumentException(
              "Could not construct split allocation proof: invalid path.");
    }

    if (!Arrays.equals(((FinalizedLeafBranch) node).getKey(), key)) {
      throw new IllegalArgumentException(
              String.format("Leaf not found for key: %s", HexConverter.encode(key)));
    }

    Collections.reverse(siblings);
    return new SplitAllocationProof(siblings);
  }

  /**
   * Create SplitAllocationProof from CBOR bytes.
   *
   * @param bytes CBOR bytes (an array of sibling entries)
   * @return decoded proof
   * @throws CborSerializationException on too many entries, an out-of-range or non-decreasing
   *     depth, or a non-positive or non-minimal sum
   */
  public static SplitAllocationProof fromCbor(byte[] bytes) {
    List<byte[]> entries = CborDeserializer.decodeArray(bytes);
    if (entries.size() > SplitAllocationProof.MAX_SIBLINGS) {
      throw new CborSerializationException(
              "A split allocation proof has at most 256 sibling entries.");
    }

    List<Sibling> siblings = new ArrayList<>();
    for (byte[] entry : entries) {
      List<byte[]> fields = CborDeserializer.decodeArray(entry, 3);

      int depth = CborDeserializer.decodeUnsignedInteger(fields.get(0)).asListSize();
      if (depth > SplitAllocationProof.MAX_DEPTH) {
        throw new CborSerializationException("Sibling depth must be in the range [0, 255].");
      }
      if (!siblings.isEmpty() && depth >= siblings.get(siblings.size() - 1).depth) {
        throw new CborSerializationException(
                "Sibling depths must be strictly decreasing from the leaf to the root.");
      }

      BigInteger sum = CborDeserializer.decodeBigInteger(fields.get(2), 32);
      if (sum.signum() <= 0) {
        throw new CborSerializationException("Sibling sum must be strictly positive.");
      }

      siblings.add(new Sibling(
              depth,
              new DataHash(HashAlgorithm.SHA256, CborDeserializer.decodeByteString(fields.get(1))),
              sum
      ));
    }

    return new SplitAllocationProof(siblings);
  }

  /**
   * Reconstruct the root digest and sum for this proof by hashing from the leaf upward.
   *
   * @param key 32-byte output token identifier
   * @param data 32-byte output commitment
   * @param value strictly positive output amount for the asset
   * @return reconstructed root digest and sum
   * @throws IllegalArgumentException if the inputs are structurally invalid
   * @throws ArithmeticException if the reconstructed sum overflows 256 bits
   */
  public Root calculateRoot(byte[] key, byte[] data, BigInteger value) {
    if (value.signum() <= 0 || value.compareTo(SplitAllocationProof.SUM_LIMIT) >= 0) {
      throw new IllegalArgumentException("Value must be a positive 256-bit integer.");
    }

    if (key.length != 32) {
      throw new IllegalArgumentException("Key must be 32 bytes long.");
    }

    if (data.length != 32) {
      throw new IllegalArgumentException("Data must be 32 bytes long.");
    }

    BigInteger keyPath = BitString.fromBytesReversedLSB(key).toBigInteger();

    DataHash hash = new DataHasher(HashAlgorithm.SHA256)
            .update(new byte[]{0x10})
            .update(key)
            .update(data)
            .update(BigIntegerConverter.encode(value, 32))
            .digest();
    BigInteger sum = value;

    for (Sibling sibling : this.siblings) {
      BigInteger nextSum = sum.add(sibling.sum);
      if (nextSum.compareTo(SplitAllocationProof.SUM_LIMIT) >= 0) {
        throw new ArithmeticException("Reconstructed sum overflows 256 bits.");
      }

      boolean isRight = keyPath.testBit(sibling.depth);
      DataHash leftHash = isRight ? sibling.hash : hash;
      BigInteger leftValue = isRight ? sibling.sum : sum;
      DataHash rightHash = isRight ? hash : sibling.hash;
      BigInteger rightValue = isRight ? sum : sibling.sum;

      hash = new DataHasher(HashAlgorithm.SHA256)
              .update(new byte[]{0x11, (byte) sibling.depth})
              .update(leftHash.getData())
              .update(BigIntegerConverter.encode(leftValue, 32))
              .update(rightHash.getData())
              .update(BigIntegerConverter.encode(rightValue, 32))
              .digest();
      sum = nextSum;
    }

    return new Root(hash, sum);
  }

  /**
   * Verify this proof by reconstructing the root from the leaf upward and checking it against the
   * expected root digest and target sum.
   *
   * @param key 32-byte output token identifier
   * @param data 32-byte output commitment
   * @param value strictly positive output amount for the asset
   * @param expectedRootHash expected RSMST root digest from the manifest
   * @param expectedSum expected reconstructed root sum
   * @return true if the proof reconstructs to the root and target sum
   */
  public boolean verify(byte[] key, byte[] data, BigInteger value, DataHash expectedRootHash,
                        BigInteger expectedSum) {
    try {
      Root root = this.calculateRoot(key, data, value);
      return root.getHash().equals(expectedRootHash) && root.getSum().equals(expectedSum);
    } catch (RuntimeException e) {
      return false;
    }
  }

  /**
   * Convert SplitAllocationProof to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
            this.siblings.stream()
                    .map(sibling -> CborSerializer.encodeArray(
                            CborSerializer.encodeUnsignedInteger(sibling.depth),
                            CborSerializer.encodeByteString(sibling.hash.getData()),
                            CborSerializer.encodeBigInteger(sibling.sum)
                    ))
                    .toArray(byte[][]::new)
    );
  }

  /**
   * Reconstructed RSMST root: digest and total sum.
   */
  public static final class Root {
    private final DataHash hash;
    private final BigInteger sum;

    private Root(DataHash hash, BigInteger sum) {
      this.hash = hash;
      this.sum = sum;
    }

    /**
     * Get the reconstructed root digest.
     *
     * @return root digest
     */
    public DataHash getHash() {
      return this.hash;
    }

    /**
     * Get the reconstructed root sum.
     *
     * @return root sum
     */
    public BigInteger getSum() {
      return this.sum;
    }
  }

  /**
   * One sibling entry of an explicit-depth RSMST inclusion proof.
   */
  private static final class Sibling {
    private final int depth;
    private final DataHash hash;
    private final BigInteger sum;

    private Sibling(int depth, DataHash hash, BigInteger sum) {
      this.depth = depth;
      this.hash = hash;
      this.sum = sum;
    }
  }
}
