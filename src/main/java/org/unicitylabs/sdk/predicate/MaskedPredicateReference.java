package org.unicitylabs.sdk.predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.unicitylabs.sdk.address.DirectAddress;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.TokenType;

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
