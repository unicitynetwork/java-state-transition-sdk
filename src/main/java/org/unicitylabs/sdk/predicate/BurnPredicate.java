package org.unicitylabs.sdk.predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferTransactionData;
import java.util.Arrays;
import java.util.Objects;

public class BurnPredicate implements Predicate {
  private final DataHash burnReason;
  private final byte[] nonce;

  public BurnPredicate(byte[] nonce, DataHash reason) {
    Objects.requireNonNull(nonce, "Nonce cannot be null");
    Objects.requireNonNull(reason, "Burn reason cannot be null");

    this.burnReason = reason;
    this.nonce = Arrays.copyOf(nonce, nonce.length);
  }

  public DataHash getReason() {
    return this.burnReason;
  }

  @Override
  public String getType() {
    return PredicateType.BURN.name();
  }

  // TODO: Do we need nonce for burn predicate?
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
      TokenType tokenType) {
    return false;
  }

  @Override
  public DataHash calculateHash(TokenId tokenId, TokenType tokenType) {
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.addPOJO(this.getReference(tokenType).getHash());
    node.addPOJO(tokenId);
    node.add(this.nonce);

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
