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

public class UnicityTreeCertificate {

  private final int version;
  private final int partitionIdentifier;
  private final List<HashStep> steps;

  @JsonCreator
  UnicityTreeCertificate(
      @JsonProperty("version") int version,
      @JsonProperty("partitionIdentifier") int partitionIdentifier,
      @JsonProperty("steps") List<HashStep> steps
  ) {
    Objects.requireNonNull(steps, "Steps cannot be null");

    this.version = version;
    this.partitionIdentifier = partitionIdentifier;
    this.steps = List.copyOf(steps);
  }

  @JsonGetter("version")
  public int getVersion() {
    return this.version;
  }

  @JsonGetter("partitionIdentifier")
  public int getPartitionIdentifier() {
    return this.partitionIdentifier;
  }

  @JsonGetter("steps")
  public List<HashStep> getSteps() {
    return this.steps;
  }

  public static UnicityTreeCertificate fromCbor(byte[] bytes) {
    CborTag tag = CborDeserializer.readTag(bytes);
    List<byte[]> data = CborDeserializer.readArray(tag.getData());

    return new UnicityTreeCertificate(
        CborDeserializer.readUnsignedInteger(data.get(0)).asInt(),
        CborDeserializer.readUnsignedInteger(data.get(1)).asInt(),
        CborDeserializer.readArray(data.get(2)).stream()
            .map(HashStep::fromCbor)
            .collect(Collectors.toList())
    );
  }

  public byte[] toCbor() {
    return CborSerializer.encodeTag(
        1014,
        CborSerializer.encodeArray(
            CborSerializer.encodeUnsignedInteger(this.version),
            CborSerializer.encodeUnsignedInteger(this.partitionIdentifier),
            CborSerializer.encodeArray(this.steps.stream()
                .map(HashStep::toCbor)
                .toArray(byte[][]::new))
        ));
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UnicityTreeCertificate)) {
      return false;
    }
    UnicityTreeCertificate that = (UnicityTreeCertificate) o;
    return Objects.equals(this.version, that.version) && Objects.equals(
        this.partitionIdentifier, that.partitionIdentifier) && Objects.equals(this.steps,
        that.steps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.version, this.partitionIdentifier, this.steps);
  }

  @Override
  public String toString() {
    return String.format("UnicityTreeCertificate{version=%s, partitionIdentifier=%s, steps=%s",
        this.version, this.partitionIdentifier, this.steps);
  }

  public static class HashStep {

    private final int key;
    private final byte[] hash;

    public HashStep(int key, byte[] hash) {
      Objects.requireNonNull(hash, "Hash cannot be null");

      this.key = key;
      this.hash = Arrays.copyOf(hash, hash.length);
    }

    public int getKey() {
      return this.key;
    }

    public byte[] getHash() {
      return Arrays.copyOf(this.hash, this.hash.length);
    }

    public static HashStep fromCbor(byte[] bytes) {
      List<byte[]> data = CborDeserializer.readArray(bytes);

      return new HashStep(
          CborDeserializer.readUnsignedInteger(data.get(0)).asInt(),
          CborDeserializer.readByteString(data.get(1))
      );
    }

    public byte[] toCbor() {
      return CborSerializer.encodeArray(
          CborSerializer.encodeUnsignedInteger(this.key),
          CborSerializer.encodeByteString(this.hash)
      );
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof HashStep)) {
        return false;
      }
      HashStep hashStep = (HashStep) o;
      return Objects.equals(this.key, hashStep.key) && Objects.deepEquals(this.hash,
          hashStep.hash);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.key, Arrays.hashCode(this.hash));
    }

    @Override
    public String toString() {
      return String.format("UnicityTreeCertificate.HashStep{key=%s, hash=%s",
          this.key, HexConverter.encode(this.hash));
    }
  }
}
