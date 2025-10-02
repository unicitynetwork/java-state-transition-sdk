package org.unicitylabs.sdk.token;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.BitString;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Globally unique identifier of a token.
 */
@JsonSerialize(using = TokenIdJson.Serializer.class)
@JsonDeserialize(using = TokenIdJson.Deserializer.class)
public class TokenId {

  private final byte[] bytes;

  /**
   * Create token id from bytes.
   *
   * @param bytes token id bytes
   */
  public TokenId(byte[] bytes) {
    this.bytes = Arrays.copyOf(bytes, bytes.length);
  }

  /**
   * Get token id bytes.
   *
   * @return token id bytes
   */
  public byte[] getBytes() {
    return Arrays.copyOf(this.bytes, this.bytes.length);
  }

  /**
   * Get token id as bit string.
   *
   * @return token id bit string
   */
  public BitString toBitString() {
    return new BitString(this.bytes);
  }

  /**
   * Create token id from nametag.
   *
   * @param name nametag
   * @return token id
   */
  public static TokenId fromNameTag(String name) {
    Objects.requireNonNull(name, "Name cannot be null");

    return new TokenId(
        new DataHasher(HashAlgorithm.SHA256)
            .update(name.getBytes(StandardCharsets.UTF_8))
            .digest()
            .getImprint()
    );
  }

  /**
   * Create token id from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return token id
   */
  public static TokenId fromCbor(byte[] bytes) {
    return new TokenId(CborDeserializer.readByteString(bytes));
  }

  /**
   * Convert token id to CBOR bytes.
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
    TokenId tokenId = (TokenId) o;
    return Arrays.equals(this.bytes, tokenId.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.bytes);
  }

  @Override
  public String toString() {
    return String.format("TokenId[%s]", HexConverter.encode(this.bytes));
  }
}
