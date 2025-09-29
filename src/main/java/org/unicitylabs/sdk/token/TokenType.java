
package org.unicitylabs.sdk.token;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.HexConverter;
import java.util.Arrays;

/**
 * Unique identifier describing the type/category of a token.
 */
@JsonSerialize(using = TokenTypeJson.Serializer.class)
@JsonDeserialize(using = TokenTypeJson.Deserializer.class)
public class TokenType {

  private final byte[] bytes;

  public TokenType(byte[] bytes) {
    this.bytes = Arrays.copyOf(bytes, bytes.length);
  }

  public byte[] getBytes() {
    return Arrays.copyOf(this.bytes, this.bytes.length);
  }

  public static TokenType fromCbor(byte[] bytes) {
    return new TokenType(CborDeserializer.readByteString(bytes));
  }

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
