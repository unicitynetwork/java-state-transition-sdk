package org.unicitylabs.sdk.bft;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer.CborTag;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Input record for UnicityCertificate.
 */
public class InputRecord {

  private final int version;
  private final long roundNumber;
  private final long epoch;
  private final byte[] previousHash;
  private final byte[] hash;
  private final byte[] summaryValue;
  private final long timestamp;
  private final byte[] blockHash;
  private final long sumOfEarnedFees;
  private final byte[] executedTransactionsHash;

  InputRecord(
      int version,
      long roundNumber,
      long epoch,
      byte[] previousHash,
      byte[] hash,
      byte[] summaryValue,
      long timestamp,
      byte[] blockHash,
      long sumOfEarnedFees,
      byte[] executedTransactionsHash
  ) {
    Objects.requireNonNull(hash, "Hash cannot be null");
    Objects.requireNonNull(summaryValue, "Summary value cannot be null");

    this.version = version;
    this.roundNumber = roundNumber;
    this.epoch = epoch;
    this.previousHash = previousHash;
    this.hash = hash;
    this.summaryValue = summaryValue;
    this.timestamp = timestamp;
    this.blockHash = blockHash;
    this.sumOfEarnedFees = sumOfEarnedFees;
    this.executedTransactionsHash = executedTransactionsHash;
  }

  /**
   * Get version.
   *
   * @return version
   */
  public int getVersion() {
    return this.version;
  }

  /**
   * Get round number.
   *
   * @return round number
   */
  public long getRoundNumber() {
    return this.roundNumber;
  }

  /**
   * Get epoch.
   *
   * @return epoch
   */
  public long getEpoch() {
    return this.epoch;
  }

  /**
   * Get previous hash.
   *
   * @return previous hash or null if not set
   */
  public byte[] getPreviousHash() {
    return this.previousHash != null ? Arrays.copyOf(this.previousHash, this.previousHash.length)
        : null;
  }

  /**
   * Get hash.
   *
   * @return hash
   */
  public byte[] getHash() {
    return Arrays.copyOf(this.hash, this.hash.length);
  }

  /**
   * Get summary value.
   *
   * @return summary value
   */
  public byte[] getSummaryValue() {
    return Arrays.copyOf(this.summaryValue, this.summaryValue.length);
  }

  /**
   * Get timestamp.
   *
   * @return timestamp
   */
  public long getTimestamp() {
    return this.timestamp;
  }

  /**
   * Get block hash.
   *
   * @return block hash or null if not set
   */
  public byte[] getBlockHash() {
    return this.blockHash != null ? Arrays.copyOf(this.blockHash, this.blockHash.length) : null;
  }

  /**
   * Get sum of earned fees.
   *
   * @return sum of earned fees
   */
  public long getSumOfEarnedFees() {
    return this.sumOfEarnedFees;
  }

  /**
   * Get executed transactions hash.
   *
   * @return executed transactions hash or null if not set
   */
  public byte[] getExecutedTransactionsHash() {
    return this.executedTransactionsHash != null ? Arrays.copyOf(this.executedTransactionsHash,
        this.executedTransactionsHash.length) : null;
  }

  /**
   * Create InputRecord from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return input record
   */
  public static InputRecord fromCbor(byte[] bytes) {
    CborTag tag = CborDeserializer.readTag(bytes);
    List<byte[]> data = CborDeserializer.readArray(tag.getData());

    return new InputRecord(
        CborDeserializer.readUnsignedInteger(data.get(0)).asInt(),
        CborDeserializer.readUnsignedInteger(data.get(1)).asLong(),
        CborDeserializer.readUnsignedInteger(data.get(2)).asLong(),
        CborDeserializer.readOptional(data.get(3), CborDeserializer::readByteString),
        CborDeserializer.readByteString(data.get(4)),
        CborDeserializer.readByteString(data.get(5)),
        CborDeserializer.readUnsignedInteger(data.get(6)).asLong(),
        CborDeserializer.readOptional(data.get(7), CborDeserializer::readByteString),
        CborDeserializer.readUnsignedInteger(data.get(8)).asLong(),
        CborDeserializer.readOptional(data.get(9), CborDeserializer::readByteString)
    );
  }

  /**
   * Convert InputRecord to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeTag(
        1008,
        CborSerializer.encodeArray(
            CborSerializer.encodeUnsignedInteger(this.version),
            CborSerializer.encodeUnsignedInteger(this.roundNumber),
            CborSerializer.encodeUnsignedInteger(this.epoch),
            CborSerializer.encodeOptional(this.previousHash, CborSerializer::encodeByteString),
            CborSerializer.encodeByteString(this.hash),
            CborSerializer.encodeByteString(this.summaryValue),
            CborSerializer.encodeUnsignedInteger(this.timestamp),
            CborSerializer.encodeOptional(this.blockHash, CborSerializer::encodeByteString),
            CborSerializer.encodeUnsignedInteger(this.sumOfEarnedFees),
            CborSerializer.encodeOptional(this.executedTransactionsHash,
                CborSerializer::encodeByteString)
        ));
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InputRecord)) {
      return false;
    }
    InputRecord that = (InputRecord) o;
    return Objects.equals(this.version, that.version) && Objects.equals(this.roundNumber,
        that.roundNumber) && Objects.equals(this.epoch, that.epoch)
        && Objects.deepEquals(this.previousHash, that.previousHash)
        && Objects.deepEquals(this.hash, that.hash) && Objects.deepEquals(this.summaryValue,
        that.summaryValue) && Objects.equals(this.timestamp, that.timestamp)
        && Objects.deepEquals(this.blockHash, that.blockHash) && Objects.equals(
        this.sumOfEarnedFees, that.sumOfEarnedFees) && Objects.deepEquals(
        this.executedTransactionsHash, that.executedTransactionsHash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.version, this.roundNumber, this.epoch,
        Arrays.hashCode(this.previousHash),
        Arrays.hashCode(this.hash), Arrays.hashCode(this.summaryValue), this.timestamp,
        Arrays.hashCode(this.blockHash),
        this.sumOfEarnedFees, Arrays.hashCode(this.executedTransactionsHash));
  }

  @Override
  public String toString() {
    return String.format("InputRecord{version=%s, roundNumber=%s, epoch=%s, previousHash=%s, "
            + "hash=%s, summaryValue=%s, timestamp=%s, blockHash=%s, sumOfEarnedFees=%s, "
            + "executedTransactionsHash=%s}",
        this.version,
        this.roundNumber,
        this.epoch,
        this.previousHash != null ? HexConverter.encode(this.previousHash) : null,
        HexConverter.encode(this.hash),
        HexConverter.encode(this.summaryValue),
        this.timestamp,
        this.blockHash != null ? HexConverter.encode(this.blockHash) : null,
        this.sumOfEarnedFees,
        this.executedTransactionsHash != null ? HexConverter.encode(this.executedTransactionsHash)
            : null
    );
  }
}
