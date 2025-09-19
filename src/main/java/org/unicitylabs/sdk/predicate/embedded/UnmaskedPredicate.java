
package org.unicitylabs.sdk.predicate.embedded;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.PredicateEngineType;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.signing.Signature;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.transaction.InclusionProof;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferTransactionData;

public class UnmaskedPredicate extends DefaultPredicate {

  public UnmaskedPredicate(
      TokenId tokenId,
      TokenType tokenType,
      byte[] publicKey,
      String signingAlgorithm,
      HashAlgorithm hashAlgorithm,
      byte[] nonce
  ) {
    super(EmbeddedPredicateType.UNMASKED, tokenId, tokenType, publicKey, signingAlgorithm,
        hashAlgorithm, nonce);
  }

  public static UnmaskedPredicate create(
      TokenId tokenId,
      TokenType tokenType,
      SigningService signingService,
      HashAlgorithm hashAlgorithm,
      byte[] salt
  ) {
    Signature nonce = signingService.sign(
        new DataHasher(HashAlgorithm.SHA256).update(salt).digest());

    return new UnmaskedPredicate(
        tokenId,
        tokenType,
        signingService.getPublicKey(),
        signingService.getAlgorithm(),
        hashAlgorithm,
        nonce.getBytes());
  }

  @Override
  public boolean verify(Token<?> token, Transaction<TransferTransactionData> transaction) {
    return super.verify(token, transaction) && SigningService.verifyWithPublicKey(
        new DataHasher(HashAlgorithm.SHA256)
            .update(
                token.getTransactions().isEmpty()
                    ? token.getGenesis().getData().getSalt()
                    : token.getTransactions().getLast().getData().getSalt()
            )
            .digest(),
        this.getNonce(),
        this.getPublicKey()
    );
  }

  public UnmaskedPredicateReference getReference() {
    return UnmaskedPredicateReference.create(
        this.getTokenType(),
        this.getSigningAlgorithm(),
        this.getPublicKey(),
        this.getHashAlgorithm()
    );
  }
}
