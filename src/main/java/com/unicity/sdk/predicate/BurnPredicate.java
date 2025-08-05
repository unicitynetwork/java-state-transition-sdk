package com.unicity.sdk.predicate;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferTransactionData;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class BurnPredicate implements Predicate {
  private final byte[] nonce;
  private final DataHash burnReason;

  public BurnPredicate(byte[] nonce, DataHash reason) {
    Objects.requireNonNull(nonce, "Nonce cannot be null");
    Objects.requireNonNull(reason, "Burn reason cannot be null");

    this.nonce = Arrays.copyOf(nonce, nonce.length);
    this.burnReason = reason;
  }

  public static BurnPredicate create(byte[] nonce, DataHash reason) {
    return new BurnPredicate(nonce, reason);
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
    return Arrays.copyOf(this.nonce, this.nonce.length);
  }

  @Override
  public boolean isOwner(byte[] publicKey) {
    return false;
  }

  @Override
  public boolean verify(Transaction<TransferTransactionData> transaction, TokenId tokenId,
      TokenType tokenType) throws IOException {
    return false;
  }

  @Override
  public DataHash calculateHash(TokenId tokenId, TokenType tokenType) throws IOException {
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.addPOJO(this.getReference(tokenType).getHash());
    node.addPOJO(tokenId);

    return new DataHasher(HashAlgorithm.SHA256)
        .update(UnicityObjectMapper.CBOR.writeValueAsBytes(node))
        .digest();
  }

  @Override
  public BurnPredicateReference getReference(TokenType tokenType) throws IOException {
    return BurnPredicateReference.create(tokenType, this.burnReason);
  }
}
