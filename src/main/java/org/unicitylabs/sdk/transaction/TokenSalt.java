package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.HexConverter;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;


/**
 * 32-byte salt mixed with a network identifier to derive a {@link TokenId}.
 */
public class TokenSalt {

  public static final int LENGTH = 32;

  private static final SecureRandom RANDOM = new SecureRandom();
  private final byte[] bytes;

  private TokenSalt(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Wrap an existing 32-byte salt.
   *
   * @param bytes salt bytes; must be exactly 32 bytes
   *
   * @return token salt
   */
  public static TokenSalt fromBytes(byte[] bytes) {
    Objects.requireNonNull(bytes, "Token salt cannot be null");
    if (bytes.length != TokenSalt.LENGTH) {
      throw new IllegalArgumentException(
              "Token salt must be " + TokenSalt.LENGTH + " bytes long, got " + bytes.length);
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
