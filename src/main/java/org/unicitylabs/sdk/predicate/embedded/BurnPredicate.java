package org.unicitylabs.sdk.predicate.embedded;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Objects;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.PredicateEngineType;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferTransactionData;

public class BurnPredicate implements Predicate {

  private final TokenId tokenId;
  private final TokenType tokenType;
  private final DataHash burnReason;

  public BurnPredicate(TokenId tokenId, TokenType tokenType, DataHash reason) {
    Objects.requireNonNull(tokenId, "Token id cannot be null");
    Objects.requireNonNull(tokenType, "Token type cannot be null");
    Objects.requireNonNull(reason, "Burn reason cannot be null");

    this.tokenId = tokenId;
    this.tokenType = tokenType;
    this.burnReason = reason;
  }

  public TokenId getTokenId() {
    return this.tokenId;
  }

  public TokenType getTokenType() {
    return this.tokenType;
  }

  public DataHash getReason() {
    return this.burnReason;
  }

  @Override
  public boolean isOwner(byte[] publicKey) {
    return false;
  }

  @Override
  public boolean verify(Token<?> token, Transaction<TransferTransactionData> transaction) {
    return false;
  }

  @Override
  public DataHash calculateHash() {
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.addPOJO(this.getReference().getHash());
    node.addPOJO(this.tokenId);

    try {
      return new DataHasher(HashAlgorithm.SHA256)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(node))
          .digest();
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
  }

  @Override
  public BurnPredicateReference getReference() {
    return BurnPredicateReference.create(this.tokenType, this.burnReason);
  }

  @Override
  public PredicateEngineType getEngine() {
    return PredicateEngineType.EMBEDDED;
  }

  @Override
  public byte[] encode() {
    return EmbeddedPredicateType.BURN.getBytes();
  }

  @Override
  public byte[] encodeParameters() {
    try {
      return UnicityObjectMapper.CBOR.writeValueAsBytes(this);
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
  }

  @Override
  public String toString() {
    return String.format("BurnPredicate{tokenId=%s, tokenType=%s, burnReason=%s}", this.tokenId,
        this.tokenType, this.burnReason);
  }
}
