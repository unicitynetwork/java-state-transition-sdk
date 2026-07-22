package org.unicitylabs.sdk.util;

import java.math.BigInteger;

/**
 * BigInteger converter to bytes and back.
 */
public class BigIntegerConverter {

  private BigIntegerConverter() {
  }

  /**
   * Decode bytes to BigInteger.
   *
   * @param data bytes
   * @return BigInteger
   */
  public static BigInteger decode(byte[] data) {
    return BigIntegerConverter.decode(data, 0, data.length);
  }

  /**
   * Decode bytes to BigInteger for byte range.
   *
   * @param data   bytes
   * @param offset offset position
   * @param length length
   * @return BigInteger
   */
  public static BigInteger decode(byte[] data, int offset, int length) {
    if (offset < 0 || length < 0 || offset + length > data.length) {
      throw new Error("Index out of bounds");
    }
    BigInteger t = BigInteger.ZERO;
    for (int i = 0; i < length; ++i) {
      t = t.shiftLeft(8).or(BigInteger.valueOf(data[offset + i] & 0xFF));
    }

    return t;
  }

  /**
   * Encode BigInteger to a fixed-width big-endian byte array, left-padded with zeroes.
   *
   * @param value BigInteger
   * @param length output length in bytes
   * @return bytes of exactly {@code length} bytes
   * @throws IllegalArgumentException if the value does not fit in {@code length} bytes
   */
  public static byte[] encode(BigInteger value, int length) {
    byte[] minimal = BigIntegerConverter.encode(value);
    if (minimal.length > length) {
      throw new IllegalArgumentException(
              String.format("Value does not fit in %d bytes.", length));
    }

    byte[] result = new byte[length];
    System.arraycopy(minimal, 0, result, length - minimal.length, minimal.length);
    return result;
  }

  /**
   * Encode BigInteger to bytes.
   *
   * @param value BigInteger
   * @return bytes
   */
  public static byte[] encode(BigInteger value) {
    int length = 0;
    BigInteger t = value;
    while (t.compareTo(BigInteger.ZERO) > 0) {
      t = t.shiftRight(8);
      length++;
    }

    byte[] result = new byte[length];
    t = value;
    for (int i = length - 1; i >= 0; i--) {
      result[i] = t.and(BigInteger.valueOf(0xFF)).byteValue();
      t = t.shiftRight(8);
    }

    return result;
  }
}
