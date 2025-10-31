
package org.unicitylabs.sdk.predicate.embedded;

import java.util.List;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.signing.Signature;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.transaction.TransferTransaction;

/**
 * Unmasked predicate.
 */
public class UnmaskedPredicate extends DefaultPredicate {

  UnmaskedPredicate(
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

  /**
   * Create unmasked predicate.
   *
   * @param tokenId        token id
   * @param tokenType      token type
   * @param signingService signing service
   * @param hashAlgorithm  hash algorithm
   * @param salt           received transaction salt
   * @return unmasked predicate
   */
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

  /**
   * Verify token state for current transaction.
   *
   * @param token       current token state
   * @param transaction current transaction
   * @param trustBase   trust base to verify against.
   * @return true if successful
   */
  @Override
  public boolean verify(
      Token<?> token,
      TransferTransaction transaction,
      RootTrustBase trustBase
  ) {
    List<TransferTransaction> transactions = token.getTransactions();

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

  /**
   * Create predicate from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return predicate
   */
  public static UnmaskedPredicate fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new UnmaskedPredicate(
        TokenId.fromCbor(data.get(0)),
        TokenType.fromCbor(data.get(1)),
        CborDeserializer.readByteString(data.get(2)),
        CborDeserializer.readTextString(data.get(3)),
        HashAlgorithm.fromValue(CborDeserializer.readUnsignedInteger(data.get(4)).asInt()),
        CborDeserializer.readByteString(data.get(5))
    );
  }

  /**
   * Convert predicate to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public UnmaskedPredicateReference getReference() {
    return UnmaskedPredicateReference.create(
        this.getTokenType(),
        this.getSigningAlgorithm(),
        this.getPublicKey(),
        this.getHashAlgorithm()
    );
  }
}
