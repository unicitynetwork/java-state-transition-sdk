package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.util.HexConverter;
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

  public static LeafValue create(Authenticator authenticator, DataHash transactionHash) {
    try {
      DataHash hash = new DataHasher(HashAlgorithm.SHA256)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(authenticator))
          .update(transactionHash.getImprint())
          .digest();

      return new LeafValue(hash.getImprint());
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
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