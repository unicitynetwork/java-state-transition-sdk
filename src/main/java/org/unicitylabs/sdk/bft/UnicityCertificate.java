package org.unicitylabs.sdk.bft;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer.CborTag;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.util.HexConverter;

public class UnicityCertificate {

  private final int version;
  private final InputRecord inputRecord;
  private final byte[] technicalRecordHash;
  private final byte[] shardConfigurationHash;
  private final ShardTreeCertificate shardTreeCertificate;
  private final UnicityTreeCertificate unicityTreeCertificate;
  private final UnicitySeal unicitySeal;

  @JsonCreator
  UnicityCertificate(
      @JsonProperty("version") int version,
      @JsonProperty("inputRecord") InputRecord inputRecord,
      @JsonProperty("technicalRecordHash") byte[] technicalRecordHash,
      @JsonProperty("shardConfigurationHash") byte[] shardConfigurationHash,
      @JsonProperty("shardTreeCertificate") ShardTreeCertificate shardTreeCertificate,
      @JsonProperty("unicityTreeCertificate") UnicityTreeCertificate unicityTreeCertificate,
      @JsonProperty("unicitySeal") UnicitySeal unicitySeal
  ) {
    Objects.requireNonNull(inputRecord, "Input record cannot be null");
    Objects.requireNonNull(shardConfigurationHash, "Shard configuration hash cannot be null");
    Objects.requireNonNull(shardTreeCertificate, "Shard tree certificate cannot be null");
    Objects.requireNonNull(unicityTreeCertificate, "Unicity tree certificate cannot be null");
    Objects.requireNonNull(unicitySeal, "Unicity seal cannot be null");

    this.version = version;
    this.inputRecord = inputRecord;
    this.technicalRecordHash = Arrays.copyOf(technicalRecordHash, technicalRecordHash.length);
    this.shardConfigurationHash = Arrays.copyOf(
        shardConfigurationHash,
        shardConfigurationHash.length
    );
    this.shardTreeCertificate = shardTreeCertificate;
    this.unicityTreeCertificate = unicityTreeCertificate;
    this.unicitySeal = unicitySeal;
  }

  @JsonGetter("version")
  public int getVersion() {
    return this.version;
  }

  @JsonGetter("inputRecord")
  public InputRecord getInputRecord() {
    return this.inputRecord;
  }

  @JsonGetter("technicalRecordHash")
  public byte[] getTechnicalRecordHash() {
    return Arrays.copyOf(this.technicalRecordHash, this.technicalRecordHash.length);
  }

  @JsonGetter("shardConfigurationHash")
  public byte[] getShardConfigurationHash() {
    return Arrays.copyOf(this.shardConfigurationHash, this.shardConfigurationHash.length);
  }

  @JsonGetter("shardTreeCertificate")
  public ShardTreeCertificate getShardTreeCertificate() {
    return this.shardTreeCertificate;
  }

  @JsonGetter("unicityTreeCertificate")
  public UnicityTreeCertificate getUnicityTreeCertificate() {
    return this.unicityTreeCertificate;
  }

  @JsonGetter("unicitySeal")
  public UnicitySeal getUnicitySeal() {
    return this.unicitySeal;
  }

  public static DataHash calculateShardTreeCertificateRootHash(
      InputRecord inputRecord,
      byte[] technicalRecordHash,
      byte[] shardConfigurationHash,
      ShardTreeCertificate shardTreeCertificate
  ) {

    DataHash rootHash = new DataHasher(HashAlgorithm.SHA256)
        .update(inputRecord.toCbor())
        .update(CborSerializer.encodeByteString(technicalRecordHash))
        .update(CborSerializer.encodeByteString(shardConfigurationHash))
        .digest();

    byte[] shardId = shardTreeCertificate.getShard();
    List<byte[]> siblingHashes = shardTreeCertificate.getSiblingHashList();
    for (int i = 0; i < siblingHashes.size(); i++) {
      boolean isRight = shardId[(shardId.length - 1) - (i / 8)] == 1;
      if (isRight) {
        rootHash = new DataHasher(HashAlgorithm.SHA256)
            .update(siblingHashes.get(i))
            .update(rootHash.getData())
            .digest();
      } else {
        rootHash = new DataHasher(HashAlgorithm.SHA256)
            .update(rootHash.getData())
            .update(siblingHashes.get(i))
            .digest();
      }
    }

    return rootHash;

  }

  public static UnicityCertificate fromCbor(byte[] bytes) {
    CborTag tag = CborDeserializer.readTag(bytes);
    List<byte[]> data = CborDeserializer.readArray(tag.getData());

    return new UnicityCertificate(
        CborDeserializer.readUnsignedInteger(data.get(0)).asInt(),
        InputRecord.fromCbor(data.get(1)),
        CborDeserializer.readByteString(data.get(2)),
        CborDeserializer.readByteString(data.get(3)),
        ShardTreeCertificate.fromCbor(data.get(4)),
        UnicityTreeCertificate.fromCbor(data.get(5)),
        UnicitySeal.fromCbor(data.get(6))
    );
  }

  public byte[] toCbor() {
    return CborSerializer.encodeTag(
        1007,
        CborSerializer.encodeArray(
            CborSerializer.encodeUnsignedInteger(this.version),
            this.inputRecord.toCbor(),
            CborSerializer.encodeByteString(this.technicalRecordHash),
            CborSerializer.encodeByteString(this.shardConfigurationHash),
            this.shardTreeCertificate.toCbor(),
            this.unicityTreeCertificate.toCbor(),
            this.unicitySeal.toCbor()
        ));
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UnicityCertificate)) {
      return false;
    }
    UnicityCertificate that = (UnicityCertificate) o;
    return Objects.equals(this.version, that.version) && Objects.equals(this.inputRecord,
        that.inputRecord) && Objects.deepEquals(this.technicalRecordHash,
        that.technicalRecordHash) && Objects.deepEquals(this.shardConfigurationHash,
        that.shardConfigurationHash) && Objects.equals(this.shardTreeCertificate,
        that.shardTreeCertificate) && Objects.equals(this.unicityTreeCertificate,
        that.unicityTreeCertificate) && Objects.equals(this.unicitySeal, that.unicitySeal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.version, this.inputRecord, Arrays.hashCode(this.technicalRecordHash),
        Arrays.hashCode(this.shardConfigurationHash), this.shardTreeCertificate,
        this.unicityTreeCertificate, this.unicitySeal);
  }

  @Override
  public String toString() {
    return String.format("UnicityCertificate{version=%s, inputRecord=%s, technicalRecordHash=%s, "
            + "shardConfigurationHash=%s, shardTreeCertificate=%s, unicityTreeCertificate=%s, "
            + "unicitySeal=%s}",
        this.version,
        this.inputRecord,
        this.technicalRecordHash != null ? HexConverter.encode(this.technicalRecordHash) : null,
        HexConverter.encode(this.shardConfigurationHash),
        this.shardTreeCertificate,
        this.unicityTreeCertificate,
        this.unicitySeal
    );
  }
}
