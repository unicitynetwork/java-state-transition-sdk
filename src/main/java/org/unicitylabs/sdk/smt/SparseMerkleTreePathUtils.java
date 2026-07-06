package org.unicitylabs.sdk.smt;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Path utilities for the radix sparse Merkle trees.
 */
public final class SparseMerkleTreePathUtils {

  /**
   * Region size in bytes: the SMT key length, since the region holds a full 256-bit key prefix. A
   * fixed protocol constant that must match the JS SDK; it is not a tunable parameter.
   */
  private static final int REGION_LENGTH = 32;

  private SparseMerkleTreePathUtils() {
  }

  /**
   * Region committed by an interior node: the `depth`-bit common prefix of all leaves in the node's
   * sub-tree. The `i`th lowest bit of path will be the `i mod 8`th lowest bit in the `i div 8`th
   * byte of the returned 32-byte array (so the packing is little-endian); the remaining bits of the array
   * are set to zero.
   *
   * @param path absolute node or key path
   * @param depth bifurcation depth of the node
   * @return 32-byte region
   * @throws NullPointerException if {@code path} is {@code null}
   */
  public static byte[] pathToRegion(BigInteger path, int depth) {
    Objects.requireNonNull(path, "path cannot be null");

    byte[] region = new byte[REGION_LENGTH];
    int fullBytes = depth / 8;
    int remainderBits = depth % 8;

    BigInteger bits = path;
    for (int j = 0; j < fullBytes && j < REGION_LENGTH; j++, bits = bits.shiftRight(8)) {
      region[j] = bits.byteValue();
    }
    if (remainderBits > 0 && fullBytes < REGION_LENGTH) {
      region[fullBytes] = (byte) (bits.byteValue() & ((1 << remainderBits) - 1));
    }
    return region;
  }
}
