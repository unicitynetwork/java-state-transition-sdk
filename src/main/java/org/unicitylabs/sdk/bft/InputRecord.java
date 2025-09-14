package org.unicitylabs.sdk.bft;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import org.unicitylabs.sdk.util.HexConverter;

public class InputRecord {

  private final BigInteger version;
  private final BigInteger roundNumber;
  private final BigInteger epoch;
  private final byte[] previousHash;
  private final byte[] hash;
  private final byte[] summaryValue;
  private final BigInteger timestamp;
  private final byte[] blockHash;
  private final BigInteger sumOfEarnedFees;
  private final byte[] executedTransactionsHash;

  public InputRecord(
      BigInteger version,
      BigInteger roundNumber,
      BigInteger epoch,
      byte[] previousHash,
      byte[] hash,
      byte[] summaryValue,
      BigInteger timestamp,
      byte[] blockHash,
      BigInteger sumOfEarnedFees,
      byte[] executedTransactionsHash
  ) {
    Objects.requireNonNull(version, "Version cannot be null");
    Objects.requireNonNull(roundNumber, "Round number cannot be null");
    Objects.requireNonNull(epoch, "Epoch cannot be null");
    Objects.requireNonNull(summaryValue, "Summary value cannot be null");
    Objects.requireNonNull(timestamp, "Timestamp cannot be null");
    Objects.requireNonNull(sumOfEarnedFees, "Sum of earned fees cannot be null");

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
