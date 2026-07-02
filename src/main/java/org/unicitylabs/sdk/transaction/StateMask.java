package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.HexConverter;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

/**
 * 32-byte random value mixed into a transfer's next state hash. Its randomness makes the next
 * state identifier unpredictable, preventing the Unicity Service from linking consecutive states
 * of the same token, and it MUST be sampled with at least 128 bits of min-entropy.
 */
public class StateMask {

  public static final int LENGTH = 32;

  private static final SecureRandom RANDOM = new SecureRandom();
  private final byte[] bytes;

  private StateMask(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Wrap an existing 32-byte state mask.
   *
   * @param bytes state mask bytes; must be exactly 32 bytes
   *
   * @return state mask
   */
  public static StateMask fromBytes(byte[] bytes) {
    Objects.requireNonNull(bytes, "State mask cannot be null");
    if (bytes.length != StateMask.LENGTH) {
      throw new IllegalArgumentException(
              "State mask must be " + StateMask.LENGTH + " bytes long, got " + bytes.length);
    }
    return new StateMask(Arrays.copyOf(bytes, bytes.length));
  }

  /**
   * Deserialize a state mask from CBOR bytes.
   *
   * @param bytes CBOR encoded state mask bytes
   *
   * @return state mask
   */
  public static StateMask fromCbor(byte[] bytes) {
    return StateMask.fromBytes(CborDeserializer.decodeByteString(bytes));
  }

  /**
   * Generate a fresh random 32-byte state mask.
   *
   * @return state mask
   */
  public static StateMask generate() {
    byte[] bytes = new byte[StateMask.LENGTH];
    RANDOM.nextBytes(bytes);
    return new StateMask(bytes);
  }

  /**
   * Get state mask bytes.
   *
   * @return state mask bytes
   */
  public byte[] getBytes() {
    return Arrays.copyOf(this.bytes, this.bytes.length);
  }

  /**
   * Serialize state mask to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeByteString(this.bytes);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof StateMask)) {
      return false;
    }
    StateMask stateMask = (StateMask) o;
    return Arrays.equals(this.bytes, stateMask.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.bytes);
  }

  @Override
  public String toString() {
    return String.format("StateMask[%s]", HexConverter.encode(this.bytes));
  }
}
