package com.unicity.sdk.predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.token.TokenType;

public class BurnPredicateReference implements IPredicateReference {

  private final DataHash hash;

  private BurnPredicateReference(DataHash hash) {
    this.hash = hash;
  }

  public DataHash getHash() {
    return this.hash;
  }

  public static BurnPredicateReference create(TokenType tokenType, DataHash burnReason) throws JsonProcessingException {
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.add(PredicateType.BURN.name());
    node.addPOJO(tokenType);
    node.addPOJO(burnReason);

    DataHash hash = new DataHasher(HashAlgorithm.SHA256)
        .update(UnicityObjectMapper.CBOR.writeValueAsBytes(node))
        .digest();

    return new BurnPredicateReference(hash);
  }

  public DirectAddress toAddress() {
    return DirectAddress.create(this.hash);
  }
}
