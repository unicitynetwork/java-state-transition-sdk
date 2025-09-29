package org.unicitylabs.sdk.bft;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer.CborTag;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer.CborMap;
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

  @JsonCreator
  UnicitySeal(
      @JsonProperty("version") int version,
      @JsonProperty("networkId") short networkId,
      @JsonProperty("rootChainRoundNumber") long rootChainRoundNumber,
      @JsonProperty("epoch") long epoch,
      @JsonProperty("timestamp") long timestamp,
      @JsonProperty("previousHash") byte[] previousHash,
      @JsonProperty("hash") byte[] hash,
      @JsonProperty("signatures") Map<String, byte[]> signatures
  ) {
    Objects.requireNonNull(hash, "Hash cannot be null");

    this.version = version;
    this.networkId = networkId;
    this.rootChainRoundNumber = rootChainRoundNumber;
    this.epoch = epoch;
    this.timestamp = timestamp;
    this.previousHash = previousHash;
    this.hash = hash;
    this.signatures = signatures == null
        ? null
        : signatures.entrySet().stream()
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

  public UnicitySeal withSignatures(Map<String, byte[]> signatures) {
    return new UnicitySeal(
        this.version,
        this.networkId,
        this.rootChainRoundNumber,
        this.epoch,
        this.timestamp,
        this.previousHash,
        this.hash,
        signatures
    );
  }

  @JsonProperty("version")
  public int getVersion() {
    return this.version;
  }

  @JsonProperty("networkId")
  public short getNetworkId() {
    return this.networkId;
  }

  @JsonProperty("rootChainRoundNumber")
  public long getRootChainRoundNumber() {
    return this.rootChainRoundNumber;
  }

  @JsonProperty("epoch")
  public long getEpoch() {
    return this.epoch;
  }

  @JsonProperty("timestamp")
  public long getTimestamp() {
    return this.timestamp;
  }

  @JsonProperty("previousHash")
  public byte[] getPreviousHash() {
    return this.previousHash != null ? Arrays.copyOf(this.previousHash, this.previousHash.length)
        : null;
  }

  @JsonProperty("hash")
  public byte[] getHash() {
    return Arrays.copyOf(this.hash, this.hash.length);
  }

  @JsonProperty("signatures")
  public Map<String, byte[]> getSignatures() {
    return this.signatures == null
        ? null
        : this.signatures.entrySet().stream()
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

  public static UnicitySeal fromCbor(byte[] bytes) {
    CborTag tag = CborDeserializer.readTag(bytes);
    List<byte[]> data = CborDeserializer.readArray(tag.getData());

    return new UnicitySeal(
        CborDeserializer.readUnsignedInteger(data.get(0)).asInt(),
        CborDeserializer.readUnsignedInteger(data.get(1)).asShort(),
        CborDeserializer.readUnsignedInteger(data.get(2)).asLong(),
        CborDeserializer.readUnsignedInteger(data.get(3)).asLong(),
        CborDeserializer.readUnsignedInteger(data.get(4)).asLong(),
        CborDeserializer.readOptional(data.get(5), CborDeserializer::readByteString),
        CborDeserializer.readByteString(data.get(6)),
        CborDeserializer.readMap(data.get(7)).stream()
            .collect(
                Collectors.toMap(
                    entry -> CborDeserializer.readTextString(entry.getKey()),
                    entry -> CborDeserializer.readByteString(entry.getValue()
                    )
                )
            )
    );
  }

  public byte[] toCbor() {
    return CborSerializer.encodeTag(
        1001,
        CborSerializer.encodeArray(
            CborSerializer.encodeUnsignedInteger(this.version),
            CborSerializer.encodeUnsignedInteger(this.networkId),
            CborSerializer.encodeUnsignedInteger(this.rootChainRoundNumber),
            CborSerializer.encodeUnsignedInteger(this.epoch),
            CborSerializer.encodeUnsignedInteger(this.timestamp),
            CborSerializer.encodeOptional(this.previousHash, CborSerializer::encodeByteString),
            CborSerializer.encodeByteString(this.hash),
            CborSerializer.encodeOptional(
                this.signatures,
                (signatures) -> CborSerializer.encodeMap(
                    new CborMap(
                        signatures.entrySet().stream()
                            .map(entry -> new CborMap.Entry(
                                    CborSerializer.encodeTextString(entry.getKey()),
                                    CborSerializer.encodeByteString(entry.getValue())
                                )
                            )
                            .collect(Collectors.toSet())
                    )
                )
            )
        )
    );
  }

  public byte[] toCborWithoutSignatures() {
    return new UnicitySeal(
        this.version,
        this.networkId,
        this.rootChainRoundNumber,
        this.epoch,
        this.timestamp,
        this.previousHash,
        this.hash,
        null
    ).toCbor();
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
