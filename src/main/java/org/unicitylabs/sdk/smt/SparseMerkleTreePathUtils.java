package org.unicitylabs.sdk.smt;

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
   * Length of the common big-endian bit prefix shared by keys {@code a} and {@code b}, capped at
   * {@code maxDepth} (depth 0 is the most significant bit of byte 0). Used to find where a new key
   * bifurcates from an existing branch: pass {@code 256} for a leaf (compare the whole key) or the
   * node's depth for an interior branch (its stored region is only meaningful up to that depth).
   *
   * @param a first key
   * @param b second key
   * @param maxDepth cap on the returned prefix length
   * @return common-prefix bit length
   * @throws NullPointerException if {@code a} or {@code b} is {@code null}
   */
  public static int commonPrefixLength(byte[] a, byte[] b, int maxDepth) {
    Objects.requireNonNull(a, "a cannot be null");
    Objects.requireNonNull(b, "b cannot be null");

    int fullBytes = maxDepth / 8;
    for (int i = 0; i < fullBytes; i++) {
      if (a[i] != b[i]) {
        return (i * 8) + Integer.numberOfLeadingZeros((a[i] ^ b[i]) & 0xff) - 24;
      }
    }

    int remainderBits = maxDepth % 8;
    if (remainderBits > 0) {
      int diff = (a[fullBytes] ^ b[fullBytes]) & (0xff << (8 - remainderBits)) & 0xff;
      if (diff != 0) {
        return (fullBytes * 8) + Integer.numberOfLeadingZeros(diff) - 24;
      }
    }

    return maxDepth;
  }

  /**
   * The key's first {@code depth} bits, with the remaining bits of the 32-byte array zeroed.
   *
   * @param key routing key
   * @param depth bifurcation depth of the node
   * @return 32-byte region
   * @throws NullPointerException if {@code key} is {@code null}
   */
  public static byte[] regionFromKey(byte[] key, int depth) {
    Objects.requireNonNull(key, "key cannot be null");

    byte[] region = new byte[REGION_LENGTH];
    int fullBytes = depth / 8;
    int remainderBits = depth % 8;

    System.arraycopy(key, 0, region, 0, fullBytes);
    if (remainderBits > 0) {
      region[fullBytes] = (byte) (key[fullBytes] & (0xff << (8 - remainderBits)));
    }
    return region;
  }

  /**
   * Big-endian bit of {@code data} at the given depth per the Yellowpaper: depth 0 is the most
   * significant bit of {@code data[0]} ({@code data[0] & 0x80}) and depth 255 is the least
   * significant bit of {@code data[31]}.
   *
   * @param data value to read a bit from
   * @param depth bit index
   * @return {@code 0} or {@code 1}
   * @throws IllegalArgumentException if {@code depth} is out of bounds for {@code data}
   */
  public static int getBitAtDepth(byte[] data, int depth) {
    Objects.requireNonNull(data, "data cannot be null");
    if (depth < 0 || depth >= data.length * 8) {
      throw new IllegalArgumentException(
              String.format("Depth %d is out of bounds for a %d-byte value.", depth, data.length));
    }
    int byteIndex = depth / 8;
    int bitInByte = depth % 8;
    return ((data[byteIndex] & 0xff) >> (7 - bitInByte)) & 1;
  }
}
