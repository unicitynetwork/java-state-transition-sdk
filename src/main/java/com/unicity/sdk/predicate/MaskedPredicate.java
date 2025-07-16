package com.unicity.sdk.predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;

public class MaskedPredicate extends DefaultPredicate<MaskedPredicateReference> {
    private MaskedPredicate(
            byte[] publicKey,
            String algorithm,
            HashAlgorithm hashAlgorithm,
            byte[] nonce,
            MaskedPredicateReference reference,
            DataHash hash) {
        super(
                PredicateType.MASKED,
                publicKey,
                algorithm,
                hashAlgorithm,
                nonce,
                reference,
                hash
        );
    }

    public static MaskedPredicate create(TokenId tokenId, TokenType tokenType, SigningService signingService, HashAlgorithm hashAlgorithm, byte[] nonce) throws JsonProcessingException {
        return MaskedPredicate.create(
                tokenId,
                tokenType,
                signingService.getAlgorithm(),
                signingService.getPublicKey(),
                hashAlgorithm,
                nonce
        );
    }

    public static MaskedPredicate create(TokenId tokenId, TokenType tokenType, String signingAlgorithm, byte[] publicKey, HashAlgorithm hashAlgorithm, byte[] nonce) throws JsonProcessingException {
        MaskedPredicateReference reference = MaskedPredicateReference.create(tokenType, signingAlgorithm, publicKey, hashAlgorithm, nonce);
        DataHash hash = new DataHasher(HashAlgorithm.SHA256)
                .update(tokenId.getBytes())
                .update(UnicityObjectMapper.CBOR.writeValueAsBytes(reference.getHash()))
                .digest();

        return new MaskedPredicate(
                publicKey,
                signingAlgorithm,
                hashAlgorithm,
                nonce,
                reference,
                hash
        );
    }
}
