package com.unicity.sdk.predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.serializer.cbor.CborSerializationException;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.TokenType;

public class MaskedPredicateReference implements IPredicateReference {

  private final DataHash hash;

  private MaskedPredicateReference(DataHash hash) {
    this.hash = hash;
  }

  public DataHash getHash() {
    return this.hash;
  }

  public static MaskedPredicateReference create(TokenType tokenType, String signingAlgorithm,
      byte[] publicKey, HashAlgorithm hashAlgorithm, byte[] nonce) {
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.add(PredicateType.MASKED.name());
    node.addPOJO(tokenType);
    node.add(signingAlgorithm);
    node.addPOJO(hashAlgorithm);
    node.add(publicKey);
    node.add(nonce);

    try {
      DataHash hash = new DataHasher(HashAlgorithm.SHA256)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(node))
          .digest();

      return new MaskedPredicateReference(hash);
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
  }

  public static MaskedPredicateReference create(TokenType tokenType, SigningService signingService,
      HashAlgorithm hashAlgorithm, byte[] nonce) {
    return MaskedPredicateReference.create(tokenType, signingService.getAlgorithm(),
        signingService.getPublicKey(), hashAlgorithm, nonce);
  }

  public DirectAddress toAddress() {
    return DirectAddress.create(this.hash);
  }
}
