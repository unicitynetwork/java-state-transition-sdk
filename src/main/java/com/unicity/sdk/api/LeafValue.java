package com.unicity.sdk.api;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.util.HexConverter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Leaf value for merkle tree
 */
public class LeafValue {

  private final byte[] bytes;

  private LeafValue(byte[] bytes) {
    this.bytes = Arrays.copyOf(bytes, bytes.length);
  }

  public static LeafValue create(Authenticator authenticator, DataHash transactionHash)
      throws IOException {
    DataHasher hasher = new DataHasher(HashAlgorithm.SHA256);
    hasher.update(UnicityObjectMapper.CBOR.writeValueAsBytes(authenticator));
    hasher.update(transactionHash.getImprint());

    return new LeafValue(hasher.digest().getImprint());
  }

  public byte[] getBytes() {
    return Arrays.copyOf(this.bytes, this.bytes.length);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof LeafValue)) {
      return false;
    }
    LeafValue leafValue = (LeafValue) o;
    return Objects.deepEquals(this.bytes, leafValue.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.bytes);
  }

  @Override
  public String toString() {
    return String.format("LeafValue{%s}", HexConverter.encode(this.bytes));
  }
}