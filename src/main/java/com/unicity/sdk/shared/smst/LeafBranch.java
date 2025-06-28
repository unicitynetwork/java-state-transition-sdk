
package com.unicity.sdk.shared.smst;

import com.unicity.sdk.shared.hash.DataHash;

import java.math.BigInteger;

public class LeafBranch implements Branch {
    private final DataHash hash;
    private final BigInteger sum;

    public LeafBranch(DataHash hash, BigInteger sum) {
        this.hash = hash;
        this.sum = sum;
    }

    @Override
    public DataHash getHash() {
        return hash;
    }

    @Override
    public BigInteger getSum() {
        return sum;
    }
}
