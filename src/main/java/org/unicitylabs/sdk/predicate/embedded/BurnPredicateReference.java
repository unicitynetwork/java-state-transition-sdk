package org.unicitylabs.sdk.predicate.embedded;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.unicitylabs.sdk.address.DirectAddress;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.PredicateReference;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.token.TokenType;

public class BurnPredicateReference implements PredicateReference {

  private final DataHash hash;

  private BurnPredicateReference(DataHash hash) {
    this.hash = hash;
  }

  public DataHash getHash() {
    return this.hash;
  }

  public static BurnPredicateReference create(TokenType tokenType, DataHash burnReason) {
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.add(EmbeddedPredicateType.BURN.name());
    node.addPOJO(tokenType);
    node.addPOJO(burnReason);

    try {
      DataHash hash = new DataHasher(HashAlgorithm.SHA256)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(node))
          .digest();

      return new BurnPredicateReference(hash);
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
  }

  public DirectAddress toAddress() {
    return DirectAddress.create(this.hash);
  }
}
