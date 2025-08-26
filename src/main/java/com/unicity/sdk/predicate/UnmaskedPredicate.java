
package com.unicity.sdk.predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.serializer.cbor.CborSerializationException;
import com.unicity.sdk.signing.Signature;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;

public class UnmaskedPredicate extends DefaultPredicate {

  public UnmaskedPredicate(
      byte[] publicKey,
      String algorithm,
      HashAlgorithm hashAlgorithm,
      byte[] nonce) {
    super(PredicateType.UNMASKED, publicKey, algorithm, hashAlgorithm, nonce);
  }

  public static UnmaskedPredicate create(
      SigningService signingService,
      HashAlgorithm hashAlgorithm,
      byte[] salt
  ) {
    Signature nonce = signingService.sign(
        new DataHasher(HashAlgorithm.SHA256).update(salt).digest());

    return new UnmaskedPredicate(
        signingService.getPublicKey(),
        signingService.getAlgorithm(),
        hashAlgorithm,
        nonce.getBytes());
  }

  @Override
  public DataHash calculateHash(TokenId tokenId, TokenType tokenType) {
    IPredicateReference reference = this.getReference(tokenType);

    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.addPOJO(reference.getHash());
    node.addPOJO(tokenId);
    node.addPOJO(this.getNonce());

    try {
      return new DataHasher(HashAlgorithm.SHA256)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(reference.getHash()))
          .digest();
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
  }

  public UnmaskedPredicateReference getReference(TokenType tokenType) {
    return UnmaskedPredicateReference.create(
        tokenType,
        this.getAlgorithm(),
        this.getPublicKey(),
        this.getHashAlgorithm()
    );
  }
}
