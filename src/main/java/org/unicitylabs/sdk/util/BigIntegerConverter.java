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
