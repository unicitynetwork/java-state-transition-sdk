package org.unicitylabs.sdk.bft;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.unicitylabs.sdk.util.HexConverter;

public class UnicitySeal {

  private final BigInteger version;
  private final BigInteger networkId;
  private final BigInteger rootChainRoundNumber;
  private final BigInteger epoch;
  private final BigInteger timestamp;
  private final byte[] previousHash; // nullable
  private final byte[] hash;
  private final Map<String, byte[]> signatures;

  public UnicitySeal(
      BigInteger version,
      BigInteger networkId,
      BigInteger rootChainRoundNumber,
      BigInteger epoch,
      BigInteger timestamp,
      byte[] previousHash,
      byte[] hash,
      Map<String, byte[]> signatures
  ) {
    Objects.requireNonNull(version, "Version cannot be null");
    Objects.requireNonNull(networkId, "Network ID cannot be null");
    Objects.requireNonNull(rootChainRoundNumber, "Root chain round number cannot be null");
    Objects.requireNonNull(epoch, "Epoch cannot be null");
    Objects.requireNonNull(timestamp, "Timestamp cannot be null");
    Objects.requireNonNull(hash, "Hash cannot be null");
    Objects.requireNonNull(signatures, "Signatures cannot be null");

    this.version = version;
    this.networkId = networkId;
    this.rootChainRoundNumber = rootChainRoundNumber;
    this.epoch = epoch;
    this.timestamp = timestamp;
    this.previousHash = previousHash;
    this.hash = hash;
    this.signatures = signatures;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UnicitySeal)) {
      return false;
    }
    UnicitySeal that = (UnicitySeal) o;
    return Objects.equals(this.version, that.version) && Objects.equals(this.networkId,
        that.networkId) && Objects.equals(this.rootChainRoundNumber, that.rootChainRoundNumber)
        && Objects.equals(this.epoch, that.epoch) && Objects.equals(this.timestamp,
        that.timestamp) && Objects.deepEquals(this.previousHash, that.previousHash)
        && Objects.deepEquals(this.hash, that.hash) && Objects.equals(this.signatures,
        that.signatures);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.version, this.networkId, this.rootChainRoundNumber, this.epoch,
        this.timestamp,
        Arrays.hashCode(this.previousHash), Arrays.hashCode(this.hash), this.signatures);
  }

  @Override
  public String toString() {
    return String.format(
        "UnicitySeal{version=%s, networkId=%s, rootChainRoundNumber=%s, epoch=%s, timestamp=%s, "
            + "previousHash=%s, hash=%s, signatures=%s",
        this.version,
        this.networkId,
        this.rootChainRoundNumber,
        this.epoch,
        this.timestamp,
        this.previousHash != null ? HexConverter.encode(this.previousHash) : null,
        HexConverter.encode(this.hash),
        this.signatures.entrySet()
            .stream()
            .map(entry -> String.format("%s: %s", entry.getKey(), HexConverter.encode(entry.getValue())))
            .collect(Collectors.toList())
    );
  }
}
