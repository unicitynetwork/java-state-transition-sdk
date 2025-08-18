package com.unicity.sdk.predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.serializer.cbor.CborSerializationException;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferTransactionData;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class BurnPredicate implements Predicate {
  private final DataHash burnReason;

  public BurnPredicate(DataHash reason) {
    Objects.requireNonNull(reason, "Burn reason cannot be null");

    this.burnReason = reason;
  }

  public DataHash getReason() {
    return this.burnReason;
  }

  @Override
  public String getType() {
    return PredicateType.BURN.name();
  }

  @Override
  public byte[] getNonce() {
    return new byte[0];
  }

  @Override
  public boolean isOwner(byte[] publicKey) {
    return false;
  }

  @Override
  public boolean verify(Transaction<TransferTransactionData> transaction, TokenId tokenId,
      TokenType tokenType) {
    return false;
  }

  @Override
  public DataHash calculateHash(TokenId tokenId, TokenType tokenType) {
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.addPOJO(this.getReference(tokenType).getHash());
    node.addPOJO(tokenId);

    try {
      return new DataHasher(HashAlgorithm.SHA256)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(node))
          .digest();
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
  }

  @Override
  public BurnPredicateReference getReference(TokenType tokenType) {
    return BurnPredicateReference.create(tokenType, this.burnReason);
  }
}
