
package com.unicity.sdk.shared.smst;

import com.unicity.sdk.shared.hash.DataHash;

import java.math.BigInteger;

public class MerkleSumTreeRootNode {
    private final DataHash hash;
    private final BigInteger sum;

    public MerkleSumTreeRootNode(DataHash hash, BigInteger sum) {
        this.hash = hash;
        this.sum = sum;
    }

    public DataHash getHash() {
        return hash;
    }

    public BigInteger getSum() {
        return sum;
    }
}
