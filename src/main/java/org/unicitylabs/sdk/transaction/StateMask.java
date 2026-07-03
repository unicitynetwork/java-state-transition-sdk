package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.HexConverter;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

/**
 * Random value mixed into a transfer's next state hash. Its randomness makes the next state
 * identifier unpredictable, preventing the Unicity Service from linking consecutive states of the
 * same token. Per the yellowpaper the mask is variable length ({@code x <- {0,1}^l}) and MUST be
 * sampled with at least 128 bits of min-entropy, so the minter chooses the length within
 * {@code [{@link #MIN_LENGTH}, {@link #MAX_LENGTH}]} bytes; the upper bound keeps an untrusted token
 * blob from carrying an arbitrarily large mask. {@link #generate()} samples {@link #LENGTH} bytes.
 */
public class StateMask {

  public static final int LENGTH = 32;
  public static final int MAX_LENGTH = 64;
  public static final int MIN_LENGTH = 16;

  private static final SecureRandom RANDOM = new SecureRandom();
  private final byte[] bytes;

  private StateMask(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Wrap an existing state mask. The mask is variable length but must carry at least 128 bits of
   * min-entropy and stay within the upper bound, so it must be between {@link StateMask#MIN_LENGTH}
   * and {@link StateMask#MAX_LENGTH} bytes.
   *
   * @param bytes state mask bytes; must be 16 to 64 bytes
   *
   * @return state mask
   */
  public static StateMask fromBytes(byte[] bytes) {
    Objects.requireNonNull(bytes, "State mask cannot be null");
    if (bytes.length < StateMask.MIN_LENGTH || bytes.length > StateMask.MAX_LENGTH) {
      throw new IllegalArgumentException(
              "StateMask must be between " + StateMask.MIN_LENGTH + " and " + StateMask.MAX_LENGTH
                      + " bytes, got " + bytes.length + ".");
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
