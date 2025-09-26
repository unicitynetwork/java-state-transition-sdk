package org.unicitylabs.sdk.predicate.embedded;

import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;

public class MaskedPredicate extends DefaultPredicate {

  public MaskedPredicate(
      TokenId tokenId,
      TokenType tokenType,
      byte[] publicKey,
      String signingAlgorithm,
      HashAlgorithm hashAlgorithm,
      byte[] nonce) {
    super(
        EmbeddedPredicateType.MASKED,
        tokenId,
        tokenType,
        publicKey,
        signingAlgorithm,
        hashAlgorithm,
        nonce
    );
  }

  public static MaskedPredicate create(
      TokenId tokenId,
      TokenType tokenType,
      SigningService signingService,
      HashAlgorithm hashAlgorithm,
      byte[] nonce) {
    return new MaskedPredicate(tokenId, tokenType, signingService.getPublicKey(),
        signingService.getAlgorithm(), hashAlgorithm, nonce);
  }

  @Override
  public MaskedPredicateReference getReference() {
    return MaskedPredicateReference.create(
        this.getTokenType(),
        this.getSigningAlgorithm(),
        this.getPublicKey(),
        this.getHashAlgorithm(),
        this.getNonce()
    );
  }
}
