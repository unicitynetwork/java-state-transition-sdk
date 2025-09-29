package org.unicitylabs.sdk.token;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.EncodedPredicate;
import org.unicitylabs.sdk.predicate.SerializablePredicate;
import org.unicitylabs.sdk.predicate.SerializablePredicateJson;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Represents a snapshot of token ownership and associated data.
 */
public class TokenState {

  private final SerializablePredicate predicate;
  private final byte[] data;

  @JsonCreator
  public TokenState(
      @JsonSerialize(using = SerializablePredicateJson.Serializer.class)
      @JsonDeserialize(using = SerializablePredicateJson.Deserializer.class)
      @JsonProperty("predicate") SerializablePredicate predicate,
      @JsonProperty("data") byte[] data
  ) {
    Objects.requireNonNull(predicate, "Predicate cannot be null");

    this.predicate = predicate;
    this.data = data != null ? Arrays.copyOf(data, data.length) : null;
  }

  public SerializablePredicate getPredicate() {
    return this.predicate;
  }

  public Optional<byte[]> getData() {
    return this.data != null
        ? Optional.of(Arrays.copyOf(this.data, this.data.length))
        : Optional.empty();
  }

  public DataHash calculateHash() {
    return new DataHasher(HashAlgorithm.SHA256)
        .update(this.toCbor())
        .digest();
  }

  public static TokenState fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new TokenState(
        EncodedPredicate.fromCbor(data.get(0)),
        CborDeserializer.readOptional(data.get(1), CborDeserializer::readByteString)
    );
  }

  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        CborSerializer.encodeArray(
            CborSerializer.encodeUnsignedInteger(this.predicate.getEngine().ordinal()),
            this.predicate.encode(),
            this.predicate.encodeParameters()
        ),
        CborSerializer.encodeOptional(this.data, CborSerializer::encodeByteString)
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TokenState)) {
      return false;
    }
    TokenState that = (TokenState) o;
    return Arrays.equals(this.predicate.encode(), that.predicate.encode())
        && Arrays.equals(this.predicate.encodeParameters(), that.predicate.encodeParameters())
        && Objects.equals(this.predicate.getEngine(), that.predicate.getEngine())
        && Objects.deepEquals(this.data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.predicate, Arrays.hashCode(this.data));
  }

  @Override
  public String toString() {
    return String.format(
        "TokenState{predicate=%s, data=%s}",
        this.predicate,
        this.data != null ? HexConverter.encode(this.data) : "null"
    );
  }
}