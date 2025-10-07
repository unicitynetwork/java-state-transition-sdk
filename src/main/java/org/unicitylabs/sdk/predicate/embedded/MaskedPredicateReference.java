package org.unicitylabs.sdk.predicate.embedded;

import java.util.Objects;
import org.unicitylabs.sdk.address.DirectAddress;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.PredicateReference;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.TokenType;

/**
 * Masked predicate reference.
 */
public class MaskedPredicateReference implements PredicateReference {

  private final DataHash hash;

  private MaskedPredicateReference(DataHash hash) {
    this.hash = hash;
  }

  /**
   * Get predicate hash.
   *
   * @return predicate hash
   */
  public DataHash getHash() {
    return this.hash;
  }

  /**
   * Create masked predicate reference.
   *
   * @param tokenType        token type
   * @param signingAlgorithm signing algorithm
   * @param publicKey        predicate public key
   * @param hashAlgorithm    hash algorithm
   * @param nonce            predicate nonce
   * @return predicate reference
   */
  public static MaskedPredicateReference create(
      TokenType tokenType,
      String signingAlgorithm,
      byte[] publicKey,
      HashAlgorithm hashAlgorithm,
      byte[] nonce
  ) {
    Objects.requireNonNull(tokenType, "Token type cannot be null");
    Objects.requireNonNull(signingAlgorithm, "Signing algorithm cannot be null");
    Objects.requireNonNull(publicKey, "Public key cannot be null");
    Objects.requireNonNull(hashAlgorithm, "Hash algorithm cannot be null");
    Objects.requireNonNull(nonce, "Nonce cannot be null");

    return new MaskedPredicateReference(
        new DataHasher(HashAlgorithm.SHA256)
            .update(
                CborSerializer.encodeArray(
                    CborSerializer.encodeByteString(EmbeddedPredicateType.MASKED.getBytes()),
                    CborSerializer.encodeByteString(tokenType.toCbor()),
                    CborSerializer.encodeTextString(signingAlgorithm),
                    CborSerializer.encodeUnsignedInteger(hashAlgorithm.getValue()),
                    CborSerializer.encodeByteString(publicKey),
                    CborSerializer.encodeByteString(nonce)
                )
            )
            .digest()
    );
  }

  /**
   * Create predicate reference from signing service.
   *
   * @param tokenType      token type
   * @param signingService signing service
   * @param hashAlgorithm  hash algorithm
   * @param nonce          predicate nonce
   * @return predicate reference
   */
  public static MaskedPredicateReference create(TokenType tokenType, SigningService signingService,
      HashAlgorithm hashAlgorithm, byte[] nonce) {
    return MaskedPredicateReference.create(
        tokenType,
        signingService.getAlgorithm(),
        signingService.getPublicKey(),
        hashAlgorithm,
        nonce
    );
  }

  /**
   * Convert predicate reference to address.
   *
   * @return predicate address
   */
  public DirectAddress toAddress() {
    return DirectAddress.create(this.hash);
  }
}
