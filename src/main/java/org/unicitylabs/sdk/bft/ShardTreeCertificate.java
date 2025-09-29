package org.unicitylabs.sdk.bft;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer.CborTag;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.HexConverter;

public class ShardTreeCertificate {

  private final byte[] shard;
  private final List<byte[]> siblingHashList;

  @JsonCreator
  ShardTreeCertificate(
      @JsonProperty("shard") byte[] shard,
      @JsonProperty("siblingHashList") List<byte[]> siblingHashList
  ) {
    Objects.requireNonNull(shard, "Shard cannot be null");
    Objects.requireNonNull(siblingHashList, "Sibling hash list cannot be null");

    this.shard = Arrays.copyOf(shard, shard.length);
    this.siblingHashList = siblingHashList.stream()
        .map(hash -> Arrays.copyOf(hash, hash.length))
        .collect(Collectors.toList());
  }

  @JsonGetter("shard")
  public byte[] getShard() {
    return Arrays.copyOf(this.shard, this.shard.length);
  }

  @JsonGetter("siblingHashList")
  public List<byte[]> getSiblingHashList() {
    return this.siblingHashList.stream()
        .map(hash -> Arrays.copyOf(hash, hash.length))
        .collect(Collectors.toList());
  }

  public static ShardTreeCertificate fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new ShardTreeCertificate(
        CborDeserializer.readByteString(data.get(0)),
        CborDeserializer.readArray(data.get(1)).stream()
            .map(CborDeserializer::readByteString)
            .collect(Collectors.toList())
    );
  }

  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        CborSerializer.encodeByteString(this.shard),
        CborSerializer.encodeArray(
            this.siblingHashList.stream()
                .map(CborSerializer::encodeByteString)
                .toArray(byte[][]::new)
        )
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ShardTreeCertificate)) {
      return false;
    }
    ShardTreeCertificate that = (ShardTreeCertificate) o;
    return Objects.deepEquals(this.shard, that.shard) && Objects.equals(
        this.siblingHashList, that.siblingHashList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(this.shard), this.siblingHashList);
  }

  @Override
  public String toString() {
    return String.format("ShardTreeCertificate{shard=%s, siblingHashList=%s}",
        HexConverter.encode(this.shard), this.siblingHashList);
  }
}
