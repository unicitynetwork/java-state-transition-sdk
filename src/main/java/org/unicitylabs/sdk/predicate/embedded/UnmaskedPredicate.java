
package org.unicitylabs.sdk.predicate.embedded;

import java.util.List;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.signing.Signature;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
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
  public boolean verify(
      Token<?> token,
      Transaction<TransferTransactionData> transaction,
      RootTrustBase trustBase
  ) {
    List<Transaction<TransferTransactionData>> transactions = token.getTransactions();

    return super.verify(token, transaction, trustBase) && SigningService.verifyWithPublicKey(
        new DataHasher(HashAlgorithm.SHA256)
            .update(
                transactions.isEmpty()
                    ? token.getGenesis().getData().getSalt()
                    : transactions.get(transactions.size() - 1).getData().getSalt()
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
