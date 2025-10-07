
package org.unicitylabs.sdk.token;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Arrays;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Unique identifier describing the type/category of a token.
 */
@JsonSerialize(using = TokenTypeJson.Serializer.class)
@JsonDeserialize(using = TokenTypeJson.Deserializer.class)
public class TokenType {

  private final byte[] bytes;

  /**
   * Token type constructor.
   *
   * @param bytes type bytes
   */
  public TokenType(byte[] bytes) {
    this.bytes = Arrays.copyOf(bytes, bytes.length);
  }

  /**
   * Get token type as bytes.
   *
   * @return type bytes
   */
  public byte[] getBytes() {
    return Arrays.copyOf(this.bytes, this.bytes.length);
  }

  /**
   * Create token type from CBOR.
   *
   * @param bytes CBOR bytes
   * @return token type
   */
  public static TokenType fromCbor(byte[] bytes) {
    return new TokenType(CborDeserializer.readByteString(bytes));
  }

  /**
   * Convert token type to CBOR.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeByteString(this.bytes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TokenType tokenType = (TokenType) o;
    return Arrays.equals(this.bytes, tokenType.bytes);
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
