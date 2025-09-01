package com.unicity.sdk.predicate;

import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.TokenType;

public class MaskedPredicate extends DefaultPredicate {

  public MaskedPredicate(
      byte[] publicKey,
      String signingAlgorithm,
      HashAlgorithm hashAlgorithm,
      byte[] nonce) {
    super(
        PredicateType.MASKED,
        publicKey,
        signingAlgorithm,
        hashAlgorithm,
        nonce
    );
  }

  public static MaskedPredicate create(
      SigningService signingService,
      HashAlgorithm hashAlgorithm,
      byte[] nonce) {
    return new MaskedPredicate(signingService.getPublicKey(), signingService.getAlgorithm(),
        hashAlgorithm, nonce);
  }

  @Override
  public MaskedPredicateReference getReference(TokenType tokenType) {
    return MaskedPredicateReference.create(
        tokenType,
        this.getSigningAlgorithm(),
        this.getPublicKey(),
        this.getHashAlgorithm(),
        this.getNonce()
    );
  }
}
