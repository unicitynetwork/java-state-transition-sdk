package com.unicity.sdk.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.predicate.Predicate;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.serializer.cbor.CborSerializationException;
import com.unicity.sdk.util.HexConverter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a snapshot of token ownership and associated data.
 */
public class TokenState {

  private final Predicate unlockPredicate;
  private final byte[] data;

  public TokenState(Predicate unlockPredicate, byte[] data) {
    Objects.requireNonNull(unlockPredicate, "Unlock predicate cannot be null");
    this.unlockPredicate = unlockPredicate;
    this.data = data != null ? Arrays.copyOf(data, data.length) : null;
  }

  public Predicate getUnlockPredicate() {
    return this.unlockPredicate;
  }

  public Optional<byte[]> getData() {
    return this.data != null
        ? Optional.of(Arrays.copyOf(this.data, this.data.length))
        : Optional.empty();
  }

  public DataHash calculateHash(TokenId tokenId, TokenType tokenType) {
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.addPOJO(this.unlockPredicate.calculateHash(tokenId, tokenType));
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
    return Objects.equals(this.unlockPredicate, that.unlockPredicate)
        && Objects.deepEquals(this.data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.unlockPredicate, Arrays.hashCode(this.data));
  }

  @Override
  public String toString() {
    return String.format(
        "TokenState{unlockPredicate=%s, data=%s}",
        this.unlockPredicate,
        this.data != null ? HexConverter.encode(this.data) : "null"
    );
  }
}