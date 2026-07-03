package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.BitString;
import org.unicitylabs.sdk.util.HexConverter;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

/**
 * Type identifier of a token.
 */
public class TokenType {

  public static final int MIN_LENGTH = 1;
  public static final int MAX_LENGTH = 64;

  private static final SecureRandom RANDOM = new SecureRandom();
  private final byte[] bytes;

  /**
   * Create a token type from byte array.
   *
   * @param bytes token type bytes; must be between {@link #MIN_LENGTH} and {@link #MAX_LENGTH} bytes
   */
  public TokenType(byte[] bytes) {
    Objects.requireNonNull(bytes, "Token type cannot be null");
    if (bytes.length < TokenType.MIN_LENGTH || bytes.length > TokenType.MAX_LENGTH) {
      throw new IllegalArgumentException(
              String.format("Token type must be between %d and %d bytes, got %d.",
                      TokenType.MIN_LENGTH, TokenType.MAX_LENGTH, bytes.length));
    }

    this.bytes = Arrays.copyOf(bytes, bytes.length);
  }

  /**
   * Get token type bytes.
   *
   * @return token type bytes
   */
  public byte[] getBytes() {
    return Arrays.copyOf(this.bytes, this.bytes.length);
  }

  /**
   * Generate a random token type.
   *
   * @return token type
   */
  public static TokenType generate() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return new TokenType(bytes);
  }

  /**
   * Deserialize a token type from CBOR bytes.
   *
   * @param bytes CBOR encoded token type bytes
   *
   * @return token type
   */
  public static TokenType fromCbor(byte[] bytes) {
    return new TokenType(CborDeserializer.decodeByteString(bytes));
  }

  /**
   * Serialize token type to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeByteString(this.bytes);
  }

  /**
   * Convert token type to bit string.
   *
   * @return bit string
   */
  public BitString toBitString() {
    return BitString.fromBytes(this.bytes);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TokenType)) {
      return false;
    }
    TokenType tokenId = (TokenType) o;
    return Arrays.equals(this.bytes, tokenId.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.bytes);
  }

  @Override
  public String toString() {
    return String.format("TokenType[%s]", HexConverter.encode(this.bytes));
  }
}
