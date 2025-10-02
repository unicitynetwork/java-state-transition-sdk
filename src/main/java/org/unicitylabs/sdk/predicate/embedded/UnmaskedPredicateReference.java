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
 * Unmasked predicate reference.
 */
public class UnmaskedPredicateReference implements PredicateReference {

  private final DataHash hash;

  private UnmaskedPredicateReference(DataHash hash) {
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
   * Create predicate reference.
   *
   * @param tokenType        token type
   * @param signingAlgorithm signing algorithm
   * @param publicKey        predicate public key
   * @param hashAlgorithm    hash algorithm
   * @return predicate reference
   */
  public static UnmaskedPredicateReference create(
      TokenType tokenType,
      String signingAlgorithm,
      byte[] publicKey,
      HashAlgorithm hashAlgorithm
  ) {
    Objects.requireNonNull(tokenType, "Token type cannot be null");
    Objects.requireNonNull(signingAlgorithm, "Signing algorithm cannot be null");
    Objects.requireNonNull(publicKey, "Public key cannot be null");
    Objects.requireNonNull(hashAlgorithm, "Hash algorithm cannot be null");

    return new UnmaskedPredicateReference(
        new DataHasher(HashAlgorithm.SHA256)
            .update(
                CborSerializer.encodeArray(
                    CborSerializer.encodeByteString(EmbeddedPredicateType.UNMASKED.getBytes()),
                    CborSerializer.encodeByteString(tokenType.toCbor()),
                    CborSerializer.encodeTextString(signingAlgorithm),
                    CborSerializer.encodeUnsignedInteger(hashAlgorithm.getValue()),
                    CborSerializer.encodeByteString(publicKey)
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
   * @return predicate reference
   */
  public static UnmaskedPredicateReference create(
      TokenType tokenType,
      SigningService signingService,
      HashAlgorithm hashAlgorithm
  ) {
    return UnmaskedPredicateReference.create(
        tokenType,
        signingService.getAlgorithm(),
        signingService.getPublicKey(),
        hashAlgorithm
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
