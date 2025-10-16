
package org.unicitylabs.sdk.token.fungible;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Arrays;
import org.unicitylabs.sdk.util.BitString;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Coin ID representation.
 */
@JsonSerialize(using = CoinIdJson.Serializer.class)
@JsonDeserialize(using = CoinIdJson.Deserializer.class)
public class CoinId {

  private final byte[] bytes;

  /**
   * Create coin ID from bytes.
   *
   * @param bytes coin identifier bytes.
   */
  public CoinId(byte[] bytes) {
    this.bytes = Arrays.copyOf(bytes, bytes.length);
  }

  /**
   * Get coin ID bytes.
   *
   * @return coin id bytes
   */
  public byte[] getBytes() {
    return Arrays.copyOf(this.bytes, this.bytes.length);
  }

  /**
   * Convert coin ID to bit string.
   *
   * @return coin id bitstring
   */
  public BitString toBitString() {
    return new BitString(this.bytes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CoinId coinId = (CoinId) o;
    return Arrays.equals(this.bytes, coinId.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.bytes);
  }

  @Override
  public String toString() {
    return String.format("CoinId{bytes=%s}", HexConverter.encode(this.bytes));
  }
}
