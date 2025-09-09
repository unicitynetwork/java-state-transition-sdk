
package org.unicitylabs.sdk.predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.signing.Signature;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferTransactionData;

public class UnmaskedPredicate extends DefaultPredicate {

  public UnmaskedPredicate(
      byte[] publicKey,
      String signingAlgorithm,
      HashAlgorithm hashAlgorithm,
      byte[] nonce) {
    super(PredicateType.UNMASKED, publicKey, signingAlgorithm, hashAlgorithm, nonce);
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
  public boolean verify(
      List<Transaction<TransferTransactionData>> transactions,
      Token<?> token
  ) {
    return super.verify(transactions, token) && SigningService.verifyWithPublicKey(
        new DataHasher(HashAlgorithm.SHA256)
            .update(
                transactions.size() > 1
                    ? transactions.get(transactions.size() - 2).getData().getSalt()
                    : token.getGenesis().getData().getSalt()
            )
            .digest(),
        this.getNonce(),
        this.getPublicKey()
    );
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
        this.getSigningAlgorithm(),
        this.getPublicKey(),
        this.getHashAlgorithm()
    );
  }
}
