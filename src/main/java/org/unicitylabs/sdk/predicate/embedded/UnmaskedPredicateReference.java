package org.unicitylabs.sdk.predicate.embedded;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Objects;
import org.unicitylabs.sdk.address.DirectAddress;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.PredicateReference;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.TokenType;

public class UnmaskedPredicateReference implements PredicateReference {

  private final DataHash hash;

  private UnmaskedPredicateReference(DataHash hash) {
    this.hash = hash;
  }

  public DataHash getHash() {
    return this.hash;
  }

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

  public DirectAddress toAddress() {
    return DirectAddress.create(this.hash);
  }
}
