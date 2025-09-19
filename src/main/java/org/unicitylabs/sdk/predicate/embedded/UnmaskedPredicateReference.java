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
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.TokenType;

public class UnmaskedPredicateReference implements PredicateReference {

  private final DataHash hash;

  private UnmaskedPredicateReference(DataHash hash) {
    this.hash = hash;
  }

  public DataHash getHash() {
    return this.hash;
  }

  public static UnmaskedPredicateReference create(TokenType tokenType, String signingAlgorithm,
      byte[] publicKey, HashAlgorithm hashAlgorithm) {
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.add(EmbeddedPredicateType.UNMASKED.name());
    node.addPOJO(tokenType);
    node.add(signingAlgorithm);
    node.addPOJO(hashAlgorithm);
    node.add(publicKey);

    try {
      DataHash hash = new DataHasher(HashAlgorithm.SHA256)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(node))
          .digest();

      return new UnmaskedPredicateReference(hash);
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
  }

  public static UnmaskedPredicateReference create(TokenType tokenType,
      SigningService signingService, HashAlgorithm hashAlgorithm) {
    return UnmaskedPredicateReference.create(tokenType, signingService.getAlgorithm(),
        signingService.getPublicKey(), hashAlgorithm);
  }

  public DirectAddress toAddress() {
    return DirectAddress.create(this.hash);
  }
}
