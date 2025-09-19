package org.unicitylabs.sdk.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.PredicateEngineService;
import org.unicitylabs.sdk.predicate.SerializablePredicate;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Represents a snapshot of token ownership and associated data.
 */
public class TokenState {

  private final SerializablePredicate predicate;
  private final byte[] data;

  public TokenState(SerializablePredicate predicate, byte[] data) {
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
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.addPOJO(PredicateEngineService.createPredicate(this.predicate).calculateHash());
    node.add(this.data);

    try {
      return new DataHasher(HashAlgorithm.SHA256).update(
          UnicityObjectMapper.CBOR.writeValueAsBytes(node)).digest();
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
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