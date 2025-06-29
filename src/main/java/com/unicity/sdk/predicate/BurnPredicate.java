
package com.unicity.sdk.predicate;

import com.unicity.sdk.identity.IIdentity;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.transaction.Transaction;

import java.util.concurrent.CompletableFuture;

public class BurnPredicate implements IPredicate {
    private final DataHash hash;
    private final DataHash reference;

    public BurnPredicate(HashAlgorithm algorithm) {
        this.hash = DataHasher.digest(algorithm, new byte[0]);
        // Burn predicate has a zero reference
        this.reference = new DataHash(new byte[32]);
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
        // Burn predicate has no owner
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Boolean> verify(Transaction<?> transaction) {
        // TODO: Implement verification
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public Object toJSON() {
        return this;
    }

    @Override
    public byte[] toCBOR() {
        return new byte[0];
    }
}
