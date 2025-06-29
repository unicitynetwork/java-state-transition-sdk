package com.unicity.sdk.predicate;

import com.unicity.sdk.identity.IIdentity;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.transaction.Transaction;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Base class for predicates, matching TypeScript implementation
 */
public abstract class DefaultPredicate implements IPredicate {
    private final PredicateType type;
    private final byte[] publicKey;
    private final String algorithm;
    private final HashAlgorithm hashAlgorithm;
    private final byte[] nonce;
    private final DataHash reference;
    private final DataHash hash;

    protected DefaultPredicate(
            PredicateType type,
            byte[] publicKey,
            String algorithm,
            HashAlgorithm hashAlgorithm,
            byte[] nonce,
            DataHash reference,
            DataHash hash) {
        this.type = type;
        this.publicKey = Arrays.copyOf(publicKey, publicKey.length);
        this.algorithm = algorithm;
        this.hashAlgorithm = hashAlgorithm;
        this.nonce = Arrays.copyOf(nonce, nonce.length);
        this.reference = reference;
        this.hash = hash;
    }

    public PredicateType getType() {
        return type;
    }

    public byte[] getPublicKey() {
        return Arrays.copyOf(publicKey, publicKey.length);
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public HashAlgorithm getHashAlgorithm() {
        return hashAlgorithm;
    }

    public byte[] getNonce() {
        return Arrays.copyOf(nonce, nonce.length);
    }

    @Override
    public DataHash getHash() {
        return hash;
    }

    @Override
    public DataHash getReference() {
        return reference;
    }

    @Override
    public CompletableFuture<Boolean> isOwner(byte[] publicKey) {
        // Default implementation - compare public keys
        return CompletableFuture.completedFuture(Arrays.equals(this.publicKey, publicKey));
    }

    @Override
    public CompletableFuture<Boolean> verify(Transaction<?> transaction) {
        // Default implementation - subclasses should override if needed
        return CompletableFuture.completedFuture(true);
    }
}