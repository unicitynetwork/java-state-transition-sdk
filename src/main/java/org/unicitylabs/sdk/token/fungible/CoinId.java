
package org.unicitylabs.sdk.token.fungible;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.unicitylabs.sdk.util.BitString;
import org.unicitylabs.sdk.util.HexConverter;
import java.util.Arrays;

public class CoinId {
  private final byte[] bytes;

  public CoinId(byte[] bytes) {
    this.bytes = Arrays.copyOf(bytes, bytes.length);
  }

  public byte[] getBytes() {
    return Arrays.copyOf(this.bytes, this.bytes.length);
  }

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
