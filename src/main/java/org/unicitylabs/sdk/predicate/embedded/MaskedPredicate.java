package org.unicitylabs.sdk.predicate.embedded;

import java.util.List;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.signing.Signature;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;

public class MaskedPredicate extends DefaultPredicate {

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

  public static MaskedPredicate create(
      TokenId tokenId,
      TokenType tokenType,
      SigningService signingService,
      HashAlgorithm hashAlgorithm,
      byte[] nonce) {
    return new MaskedPredicate(tokenId, tokenType, signingService.getPublicKey(),
        signingService.getAlgorithm(), hashAlgorithm, nonce);
  }

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
