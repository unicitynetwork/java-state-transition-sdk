package com.unicity.sdk.predicate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.JavaDataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.signing.ISignature;
import com.unicity.sdk.shared.signing.ISigningService;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class MaskedPredicate extends DefaultPredicate {
    private static final PredicateType TYPE = PredicateType.MASKED;

    /**
     * Private constructor matching TypeScript implementation
     */
    private MaskedPredicate(
            byte[] publicKey,
            String algorithm,
            HashAlgorithm hashAlgorithm,
            byte[] nonce,
            DataHash reference,
            DataHash hash) {
        super(TYPE, publicKey, algorithm, hashAlgorithm, nonce, reference, hash);
    }

    /**
     * Create a new masked predicate for the given owner.
     * @param tokenId token ID.
     * @param tokenType token type.
     * @param signingService Token owner's signing service.
     * @param hashAlgorithm Hash algorithm used to hash transaction.
     * @param nonce Nonce value used during creation, providing uniqueness.
     */
    public static CompletableFuture<MaskedPredicate> create(
            TokenId tokenId,
            TokenType tokenType,
            ISigningService<? extends ISignature> signingService,
            HashAlgorithm hashAlgorithm,
            byte[] nonce) {
        return createFromPublicKey(
                tokenId,
                tokenType,
                signingService.getAlgorithm(),
                signingService.getPublicKey(),
                hashAlgorithm,
                nonce
        );
    }

    public static CompletableFuture<MaskedPredicate> createFromPublicKey(
            TokenId tokenId,
            TokenType tokenType,
            String signingAlgorithm,
            byte[] publicKey,
            HashAlgorithm hashAlgorithm,
            byte[] nonce) {
        
        return calculateReference(tokenType, signingAlgorithm, publicKey, hashAlgorithm, nonce)
            .thenCompose(reference -> 
                calculateHash(reference, tokenId)
                    .thenApply(hash -> 
                        new MaskedPredicate(publicKey, signingAlgorithm, hashAlgorithm, nonce, reference, hash)
                    )
            );
    }

    private static CompletableFuture<DataHash> calculateReference(
            TokenType tokenType,
            String signingAlgorithm,
            byte[] publicKey,
            HashAlgorithm hashAlgorithm,
            byte[] nonce) {
        
        JavaDataHasher hasher = new JavaDataHasher(HashAlgorithm.SHA256);
        
        // Build the reference data following TypeScript implementation
        hasher.update(tokenType.getBytes());
        hasher.update(signingAlgorithm.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        hasher.update(publicKey);
        hasher.update(new byte[] { (byte) hashAlgorithm.getValue() });
        hasher.update(nonce);
        
        return hasher.digest();
    }

    private static CompletableFuture<DataHash> calculateHash(DataHash reference, TokenId tokenId) {
        JavaDataHasher hasher = new JavaDataHasher(HashAlgorithm.SHA256);
        
        hasher.update(tokenId.getBytes());
        hasher.update(reference.getHash());
        
        return hasher.digest();
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("type", TYPE.name());
        
        // Add predicate data
        ObjectNode data = mapper.createObjectNode();
        data.put("publicKey", com.unicity.sdk.shared.util.HexConverter.encode(getPublicKey()));
        data.put("algorithm", getAlgorithm());
        data.put("hashAlgorithm", getHashAlgorithm().getValue());
        data.put("nonce", com.unicity.sdk.shared.util.HexConverter.encode(getNonce()));
        
        root.set("data", data);
        return root;
    }

    @Override
    public byte[] toCBOR() {
        // CBOR encoding following TypeScript structure
        return CborEncoder.encodeArray(
            CborEncoder.encodeUnsignedInteger(TYPE.getValue()),
            CborEncoder.encodeArray(
                CborEncoder.encodeByteString(getPublicKey()),
                CborEncoder.encodeTextString(getAlgorithm()),
                CborEncoder.encodeUnsignedInteger(getHashAlgorithm().getValue()),
                CborEncoder.encodeByteString(getNonce())
            )
        );
    }
}