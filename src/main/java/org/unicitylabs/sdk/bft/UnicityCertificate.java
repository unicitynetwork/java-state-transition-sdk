package org.unicitylabs.sdk.bft;

import java.util.Arrays;
import java.util.Objects;
import org.unicitylabs.sdk.util.HexConverter;

public class UnicityCertificate {

  private final int version;
  private final InputRecord inputRecord;
  private final byte[] technicalRecordHash;
  private final byte[] shardConfigurationHash;
  public final ShardTreeCertificate shardTreeCertificate;
  public final UnicityTreeCertificate unicityTreeCertificate;
  public final UnicitySeal unicitySeal;

  public UnicityCertificate(
      int version,
      InputRecord inputRecord,
      byte[] technicalRecordHash,
      byte[] shardConfigurationHash,
      ShardTreeCertificate shardTreeCertificate,
      UnicityTreeCertificate unicityTreeCertificate,
      UnicitySeal unicitySeal
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

  public int getVersion() {
    return this.version;
  }

  public InputRecord getInputRecord() {
    return this.inputRecord;
  }

  public byte[] getTechnicalRecordHash() {
    return Arrays.copyOf(this.technicalRecordHash, this.technicalRecordHash.length);
  }

  public byte[] getShardConfigurationHash() {
    return Arrays.copyOf(this.shardConfigurationHash, this.shardConfigurationHash.length);
  }

  public ShardTreeCertificate getShardTreeCertificate() {
    return this.shardTreeCertificate;
  }

  public UnicityTreeCertificate getUnicityTreeCertificate() {
    return this.unicityTreeCertificate;
  }

  public UnicitySeal getUnicitySeal() {
    return this.unicitySeal;
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
