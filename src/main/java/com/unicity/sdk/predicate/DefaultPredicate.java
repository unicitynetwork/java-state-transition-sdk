package com.unicity.sdk.predicate;

import com.unicity.sdk.identity.IIdentity;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;

public class DefaultPredicate implements IPredicate {
    private final DataHash hash;

    public DefaultPredicate(HashAlgorithm algorithm) {
        this.hash = DataHasher.digest(algorithm, new byte[0]);
    }

    @Override
    public DataHash getHash() {
        return hash;
    }

    @Override
    public IIdentity getReference() {
        return null;
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