package org.unicitylabs.sdk.bft;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.unicitylabs.sdk.util.HexConverter;

public class UnicitySeal {

  private final int version;
  private final short networkId;
  private final long rootChainRoundNumber;
  private final long epoch;
  private final long timestamp;
  private final byte[] previousHash; // nullable
  private final byte[] hash;
  private final LinkedHashMap<String, byte[]> signatures;

  public UnicitySeal(
      int version,
      short networkId,
      long rootChainRoundNumber,
      long epoch,
      long timestamp,
      byte[] previousHash,
      byte[] hash,
      Map<String, byte[]> signatures
  ) {
    Objects.requireNonNull(hash, "Hash cannot be null");
    Objects.requireNonNull(signatures, "Signatures cannot be null");

    this.version = version;
    this.networkId = networkId;
    this.rootChainRoundNumber = rootChainRoundNumber;
    this.epoch = epoch;
    this.timestamp = timestamp;
    this.previousHash = previousHash;
    this.hash = hash;
    this.signatures = signatures.entrySet().stream()
        .map(entry -> Map.entry(
                entry.getKey(),
                Arrays.copyOf(entry.getValue(), entry.getValue().length)
            )
        )
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            )
        );
  }

  public int getVersion() {
    return this.version;
  }

  public short getNetworkId() {
    return this.networkId;
  }

  public long getRootChainRoundNumber() {
    return this.rootChainRoundNumber;
  }

  public long getEpoch() {
    return this.epoch;
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  public byte[] getPreviousHash() {
    return this.previousHash != null ? Arrays.copyOf(this.previousHash, this.previousHash.length)
        : null;
  }

  public byte[] getHash() {
    return Arrays.copyOf(this.hash, this.hash.length);
  }

  public Map<String, byte[]> getSignatures() {
    return this.signatures.entrySet().stream()
        .map(entry -> Map.entry(
                entry.getKey(),
                Arrays.copyOf(entry.getValue(), entry.getValue().length)
            )
        )
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            )
        );
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
            .map(entry -> String.format("%s: %s", entry.getKey(),
                HexConverter.encode(entry.getValue())))
            .collect(Collectors.toList())
    );
  }
}
