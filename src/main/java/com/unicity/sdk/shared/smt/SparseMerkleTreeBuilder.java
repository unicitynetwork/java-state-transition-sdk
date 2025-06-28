
package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparseMerkleTreeBuilder {
    private final Map<BigInteger, DataHash> leaves = new HashMap<>();
    private final HashAlgorithm algorithm;
    private final int depth;

    public SparseMerkleTreeBuilder(HashAlgorithm algorithm, int depth) {
        this.algorithm = algorithm;
        this.depth = depth;
    }

    public void addLeaf(BigInteger index, DataHash hash) {
        leaves.put(index, hash);
    }

    public MerkleTreeRootNode build() {
        List<Branch> branches = new ArrayList<>();
        for (Map.Entry<BigInteger, DataHash> entry : leaves.entrySet()) {
            branches.add(new LeafBranch(entry.getValue()));
        }

        if (branches.isEmpty()) {
            return new MerkleTreeRootNode(new DataHash(new byte[32], algorithm));
        }

        for (int i = 0; i < depth; i++) {
            List<Branch> newBranches = new ArrayList<>();
            for (int j = 0; j < branches.size(); j += 2) {
                Branch left = branches.get(j);
                Branch right = (j + 1 < branches.size()) ? branches.get(j + 1) : new LeafBranch(new DataHash(new byte[32], algorithm));
                newBranches.add(new NodeBranch(left, right, algorithm));
            }
            branches = newBranches;
        }

        return new MerkleTreeRootNode(branches.get(0).getHash());
    }
}
