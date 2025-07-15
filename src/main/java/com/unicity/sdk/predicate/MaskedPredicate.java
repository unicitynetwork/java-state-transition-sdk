package com.unicity.sdk.predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.address.IAddress;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.signing.SigningService;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;

class MaskedPredicateReference {
    private final TokenType tokenType;
    private final String signingAlgorithm;
    private final byte[] publicKey;
    private final HashAlgorithm hashAlgorithm;
    private final byte[] nonce;

    public MaskedPredicateReference(TokenType tokenType, String signingAlgorithm, byte[] publicKey, HashAlgorithm hashAlgorithm, byte[] nonce) {
        this.tokenType = tokenType;
        this.signingAlgorithm = signingAlgorithm;
        this.publicKey = publicKey;
        this.hashAlgorithm = hashAlgorithm;
        this.nonce = nonce;
    }

    public DataHash getHash() throws JsonProcessingException {
        return new DataHasher(HashAlgorithm.SHA256)
                .update(new ObjectMapper(new CBORFactory()).writeValueAsBytes(this))
                .digest();
    }

    public IAddress toAddress() throws JsonProcessingException {
        return DirectAddress.create(this.getHash());
    }
}

public class MaskedPredicate extends DefaultPredicate {
    private MaskedPredicate(
            byte[] publicKey,
            String algorithm,
            HashAlgorithm hashAlgorithm,
            byte[] nonce,
            DataHash reference,
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

    // TODO: Catch these exceptions and return API level exception instead?
    public static MaskedPredicate create(TokenId tokenId, TokenType tokenType, String signingAlgorithm, byte[] publicKey, HashAlgorithm hashAlgorithm, byte[] nonce) throws JsonProcessingException {
        MaskedPredicateReference reference = new MaskedPredicateReference(tokenType, signingAlgorithm, publicKey, hashAlgorithm, nonce);
        ObjectMapper cbor = new ObjectMapper(new CBORFactory());
        ArrayNode array = cbor.createArrayNode().addPOJO(reference).addPOJO(tokenId);

        DataHash hash = new DataHasher(HashAlgorithm.SHA256).update(cbor.writeValueAsBytes(array)).digest();

        return new MaskedPredicate(
                publicKey,
                signingAlgorithm,
                hashAlgorithm,
                nonce,
                reference.getHash(),
                hash
        );
    }
}
