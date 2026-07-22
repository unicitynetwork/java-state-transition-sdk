package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.HexConverter;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;


/**
 * Variable-length salt mixed with a network identifier to derive a {@link TokenId}. The minter
 * chooses the length within {@code [{@link #MIN_LENGTH}, {@link #MAX_LENGTH}]} bytes: at least 128
 * bits of entropy, and an upper bound so untrusted token blobs cannot carry an arbitrarily large
 * salt.
 */
public class TokenSalt {

  public static final int LENGTH = 32;
  public static final int MAX_LENGTH = 64;
  public static final int MIN_LENGTH = 16;

  private static final SecureRandom RANDOM = new SecureRandom();
  private final byte[] bytes;

  private TokenSalt(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Wrap an existing salt. The salt is variable-length but must carry at least 128 bits of entropy
   * and stay within the upper bound, so it must be between {@link TokenSalt#MIN_LENGTH} and
   * {@link TokenSalt#MAX_LENGTH} bytes.
   *
   * @param bytes salt bytes; must be 16 to 64 bytes
   *
   * @return token salt
   */
  public static TokenSalt fromBytes(byte[] bytes) {
    Objects.requireNonNull(bytes, "Token salt cannot be null");
    if (bytes.length < TokenSalt.MIN_LENGTH || bytes.length > TokenSalt.MAX_LENGTH) {
      throw new IllegalArgumentException(
              "TokenSalt must be between " + TokenSalt.MIN_LENGTH + " and " + TokenSalt.MAX_LENGTH
                      + " bytes, got " + bytes.length + ".");
    }
    return new TokenSalt(Arrays.copyOf(bytes, bytes.length));
  }

  /**
   * Deserialize a token salt from CBOR bytes.
   *
   * @param bytes CBOR encoded token salt bytes
   *
   * @return token salt
   */
  public static TokenSalt fromCbor(byte[] bytes) {
    return TokenSalt.fromBytes(CborDeserializer.decodeByteString(bytes));
  }

  /**
   * Generate a fresh random 32-byte token salt.
   *
   * @return token salt
   */
  public static TokenSalt generate() {
    byte[] bytes = new byte[TokenSalt.LENGTH];
    RANDOM.nextBytes(bytes);
    return new TokenSalt(bytes);
  }

  /**
   * Get token salt bytes.
   *
   * @return token salt bytes
   */
  public byte[] getBytes() {
    return Arrays.copyOf(this.bytes, this.bytes.length);
  }

  /**
   * Serialize token salt to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeByteString(this.bytes);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TokenSalt)) {
      return false;
    }
    TokenSalt tokenSalt = (TokenSalt) o;
    return Arrays.equals(this.bytes, tokenSalt.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.bytes);
  }

  @Override
  public String toString() {
    return String.format("TokenSalt[%s]", HexConverter.encode(this.bytes));
  }
}
