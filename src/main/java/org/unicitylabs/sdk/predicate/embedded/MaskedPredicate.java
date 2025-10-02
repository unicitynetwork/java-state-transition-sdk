package org.unicitylabs.sdk.predicate.embedded;

import java.util.List;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;

/**
 * Masked predicate.
 */
public class MaskedPredicate extends DefaultPredicate {

  /**
   * Create masked predicate.
   *
   * @param tokenId          token id
   * @param tokenType        token type
   * @param publicKey        predicate public key
   * @param signingAlgorithm signing algorithm
   * @param hashAlgorithm    hash algorithm
   * @param nonce            predicate nonce
   */
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

  /**
   * Create masked predicate from signing service.
   *
   * @param tokenId        token id
   * @param tokenType      token type
   * @param signingService signing service
   * @param hashAlgorithm  hash algorithm
   * @param nonce          predicate nonce
   * @return predicate
   */
  public static MaskedPredicate create(
      TokenId tokenId,
      TokenType tokenType,
      SigningService signingService,
      HashAlgorithm hashAlgorithm,
      byte[] nonce) {
    return new MaskedPredicate(tokenId, tokenType, signingService.getPublicKey(),
        signingService.getAlgorithm(), hashAlgorithm, nonce);
  }

  /**
   * Create masked predicate from CBOR bytes.
   *
   * @param bytes CBOR bytes.
   * @return predicate
   */
  public static MaskedPredicate fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new MaskedPredicate(
        TokenId.fromCbor(data.get(0)),
        TokenType.fromCbor(data.get(1)),
        CborDeserializer.readByteString(data.get(2)),
        CborDeserializer.readTextString(data.get(3)),
        HashAlgorithm.fromValue(CborDeserializer.readUnsignedInteger(data.get(4)).asInt()),
        CborDeserializer.readByteString(data.get(5))
    );
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
