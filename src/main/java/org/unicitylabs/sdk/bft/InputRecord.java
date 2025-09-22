package org.unicitylabs.sdk.bft;

import java.util.Arrays;
import java.util.Objects;
import org.unicitylabs.sdk.util.HexConverter;

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

  public InputRecord(
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

  public int getVersion() {
    return this.version;
  }

  public long getRoundNumber() {
    return this.roundNumber;
  }

  public long getEpoch() {
    return this.epoch;
  }

  public byte[] getPreviousHash() {
    return this.previousHash != null ? Arrays.copyOf(this.previousHash, this.previousHash.length) : null;
  }

  public byte[] getHash() {
    return this.hash != null ? Arrays.copyOf(this.hash, this.hash.length) : null;
  }

  public byte[] getSummaryValue() {
    return Arrays.copyOf(this.summaryValue, this.summaryValue.length);
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  public byte[] getBlockHash() {
    return this.blockHash != null ? Arrays.copyOf(this.blockHash, this.blockHash.length) : null;
  }

  public long getSumOfEarnedFees() {
    return this.sumOfEarnedFees;
  }

  public byte[] getExecutedTransactionsHash() {
    return this.executedTransactionsHash != null ? Arrays.copyOf(this.executedTransactionsHash, this.executedTransactionsHash.length) : null;
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
        this.hash != null ? HexConverter.encode(this.hash) : null,
        HexConverter.encode(this.summaryValue),
        this.timestamp,
        this.blockHash != null ? HexConverter.encode(this.blockHash) : null,
        this.sumOfEarnedFees,
        this.executedTransactionsHash != null ? HexConverter.encode(this.executedTransactionsHash) : null
    );
  }
}
