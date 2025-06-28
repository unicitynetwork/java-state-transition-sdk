
package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PendingNodeBranch implements PendingBranch {
    private final PendingBranch left;
    private final PendingBranch right;
    private final DataHash hash;

    public PendingNodeBranch(PendingBranch left, PendingBranch right, HashAlgorithm algorithm) {
        this.left = left;
        this.right = right;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(left.getHash().getHash());
            baos.write(right.getHash().getHash());
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
}
