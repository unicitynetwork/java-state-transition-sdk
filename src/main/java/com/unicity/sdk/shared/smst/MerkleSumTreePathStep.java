
package com.unicity.sdk.shared.smst;

import com.unicity.sdk.shared.hash.DataHash;

import java.math.BigInteger;

public class MerkleSumTreePathStep {
    private final DataHash hash;
    private final BigInteger sum;
    private final boolean isRight;

    public MerkleSumTreePathStep(DataHash hash, BigInteger sum, boolean isRight) {
        this.hash = hash;
        this.sum = sum;
        this.isRight = isRight;
    }

    public DataHash getHash() {
        return hash;
    }

    public BigInteger getSum() {
        return sum;
    }

    public boolean isRight() {
        return isRight;
    }
}
