package org.unicitylabs.sdk.smt;

import java.math.BigInteger;

/**
 * Path utilities for the radix sparse Merkle trees.
 */
public final class SparseMerkleTreePathUtils {

  private SparseMerkleTreePathUtils() {
  }

  /**
   * Region committed by an interior node: the node's {@code depth}-bit key prefix packed into 32
   * bytes least-significant-byte-first. {@code path} is the absolute node/key path (leading
   * sentinel bit at {@code depth}); its low {@code depth} bits are the prefix.
   *
   * @param path absolute node or key path
   * @param depth bifurcation depth of the node
   * @return 32-byte region
   */
  public static byte[] pathToRegion(BigInteger path, int depth) {
    BigInteger bits = path.and(BigInteger.ONE.shiftLeft(depth).subtract(BigInteger.ONE));
    byte[] region = new byte[32];
    for (int j = 0; j * 8 < depth; j++) {
      region[j] = (byte) bits.shiftRight(8 * j).and(BigInteger.valueOf(0xff)).intValue();
    }
    return region;
  }
}
