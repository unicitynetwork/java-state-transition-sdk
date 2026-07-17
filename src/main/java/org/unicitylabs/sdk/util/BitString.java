package org.unicitylabs.sdk.util;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Represents a bit string as a BigInteger. This class is used to ensure that leading zero bits are
 * retained when converting between byte arrays and BigInteger.
 */
public class BitString {

  private final BigInteger value;

  private BitString(byte[] data) {
    byte[] dataWithPrefix = new byte[data.length + 1];
    dataWithPrefix[0] = 1;
    System.arraycopy(data, 0, dataWithPrefix, 1, data.length);
    this.value = new BigInteger(1, dataWithPrefix);
  }

  /**
   * Creates a BitString from raw bytes with no bit reordering. BigInteger bit 0 is the LSB of the
   * last byte.
   *
   * @param data input bytes
   * @return BitString
   */
  public static BitString fromBytes(byte[] data) {
    return new BitString(Arrays.copyOf(data, data.length));
  }

  /**
   * Converts BitString to BigInteger by adding a leading byte 1 to input byte array. This is to
   * ensure that the BigInteger will retain the leading zero bits.
   *
   * @return The BigInteger representation of the bit string
   */
  public BigInteger toBigInteger() {
    return this.value;
  }

  /**
   * Converts bit string to byte array.
   *
   * @return The byte array representation of the bit string
   */
  public byte[] toBytes() {
    byte[] encoded = BigIntegerConverter.encode(this.value);
    return Arrays.copyOfRange(encoded, 1, encoded.length);
  }

  /**
   * Converts bit string to string.
   *
   * @return The string representation of the bit string in binary format
   */
  @Override
  public String toString() {
    return this.value.toString(2).substring(1);
  }
}