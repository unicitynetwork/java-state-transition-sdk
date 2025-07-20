package com.unicity.sdk.predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.token.TokenType;

public class UnmaskedPredicateReference implements IPredicateReference {

  private final DataHash hash;

  private UnmaskedPredicateReference(DataHash hash) {
    this.hash = hash;
  }

  public DataHash getHash() {
    return this.hash;
  }

  public static UnmaskedPredicateReference create(TokenType tokenType, String signingAlgorithm,
      byte[] publicKey, HashAlgorithm hashAlgorithm) throws JsonProcessingException {
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.add(PredicateType.UNMASKED.name());
    node.addPOJO(tokenType);
    node.add(signingAlgorithm);
    node.addPOJO(hashAlgorithm);
    node.add(publicKey);

    DataHash hash = new DataHasher(HashAlgorithm.SHA256)
        .update(UnicityObjectMapper.CBOR.writeValueAsBytes(node))
        .digest();

    return new UnmaskedPredicateReference(hash);
  }

  public DirectAddress toAddress() {
    return DirectAddress.create(this.hash);
  }
}
