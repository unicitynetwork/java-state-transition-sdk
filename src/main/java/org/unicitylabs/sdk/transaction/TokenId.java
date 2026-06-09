package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.api.NetworkId;
import org.unicitylabs.sdk.crypto.hash.DataHasher;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.BitString;
import org.unicitylabs.sdk.util.HexConverter;

import java.util.Arrays;
import java.util.Objects;


/**
 * Globally unique identifier of a token.
 */
public class TokenId {

  private final byte[] bytes;

  /**
   * Create a token id from byte array.
   *
   * @param bytes token id bytes
   */
  public TokenId(byte[] bytes) {
    Objects.requireNonNull(bytes, "Token id cannot be null");

    this.bytes = Arrays.copyOf(bytes, bytes.length);
  }

  /**
   * Derive a token id from a network identifier and salt.
   *
   * @param networkId network identifier
   * @param salt mint-transaction salt
   *
   * @return derived token id
   */
  public static TokenId fromSalt(NetworkId networkId, TokenSalt salt) {
    Objects.requireNonNull(networkId, "Network id cannot be null");
    Objects.requireNonNull(salt, "Token salt cannot be null");

    return new TokenId(
            new DataHasher(HashAlgorithm.SHA256)
                    .update(
                            CborSerializer.encodeArray(
                                    salt.toCbor(),
                                    CborSerializer.encodeUnsignedInteger(networkId.getId())
                            )
                    )
                    .digest()
                    .getData()
    );
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
   * Deserialize an token id from CBOR bytes.
   *
   * @param bytes CBOR encoded token id bytes
   *
   * @return token id
   */
  public static TokenId fromCbor(byte[] bytes) {
    return new TokenId(CborDeserializer.decodeByteString(bytes));
  }

  /**
   * Serialize token id to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeByteString(this.bytes);
  }

  /**
   * Convert token id to bit string.
   *
   * @return bit string
   */
  public BitString toBitString() {
    return BitString.fromBytes(this.bytes);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TokenId)) {
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
