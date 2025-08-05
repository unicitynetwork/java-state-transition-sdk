package com.unicity.sdk.predicate;

import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.TokenType;
import java.io.IOException;

public class MaskedPredicate extends DefaultPredicate {

  public MaskedPredicate(
      byte[] publicKey,
      String algorithm,
      HashAlgorithm hashAlgorithm,
      byte[] nonce) {
    super(
        PredicateType.MASKED,
        publicKey,
        algorithm,
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
  public MaskedPredicateReference getReference(TokenType tokenType) throws IOException {
    return MaskedPredicateReference.create(
        tokenType,
        this.getAlgorithm(),
        this.getPublicKey(),
        this.getHashAlgorithm(),
        this.getNonce()
    );
  }
}
