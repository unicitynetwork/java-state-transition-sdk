
package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NodeBranch implements Branch {
    private final Branch left;
    private final Branch right;
    private final DataHash hash;

    public NodeBranch(Branch left, Branch right, HashAlgorithm algorithm) {
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

    public Branch getLeft() {
        return left;
    }

    public Branch getRight() {
        return right;
    }

    @Override
    public DataHash getHash() {
        return hash;
    }
}
