
package com.unicity.sdk.shared.smst;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class PendingNodeBranch implements PendingBranch {
    private final PendingBranch left;
    private final PendingBranch right;
    private final DataHash hash;
    private final BigInteger sum;

    public PendingNodeBranch(PendingBranch left, PendingBranch right, HashAlgorithm algorithm) {
        this.left = left;
        this.right = right;
        this.sum = left.getSum().add(right.getSum());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(left.getHash().getData());
            baos.write(right.getHash().getData());
            baos.write(sum.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.hash = DataHasher.digest(algorithm, baos.toByteArray());
    }

    public PendingBranch getLeft() {
        return left;
    }

    public PendingBranch getRight() {
        return right;
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
