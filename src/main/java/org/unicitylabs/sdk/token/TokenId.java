
package org.unicitylabs.sdk.token;

import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.util.BitString;
import org.unicitylabs.sdk.util.HexConverter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Globally unique identifier of a token.
 */
public class TokenId {
    private final byte[] bytes;

    public TokenId(byte[] bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    public byte[] getBytes() {
        return Arrays.copyOf(this.bytes, this.bytes.length);
    }

    public BitString toBitString() {
        return new BitString(this.bytes);
    }

    public static TokenId fromNameTag(String name) {
      Objects.requireNonNull(name, "Name cannot be null");

      return new TokenId(
          new DataHasher(HashAlgorithm.SHA256)
              .update(name.getBytes(StandardCharsets.UTF_8))
              .digest()
              .getImprint()
      );
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
