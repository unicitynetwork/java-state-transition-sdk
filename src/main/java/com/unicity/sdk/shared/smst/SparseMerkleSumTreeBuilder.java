
package com.unicity.sdk.shared.smst;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparseMerkleSumTreeBuilder {
    private final Map<BigInteger, LeafBranch> leaves = new HashMap<>();
    private final HashAlgorithm algorithm;
    private final int depth;

    public SparseMerkleSumTreeBuilder(HashAlgorithm algorithm, int depth) {
        this.algorithm = algorithm;
        this.depth = depth;
    }

    public void addLeaf(BigInteger index, DataHash hash, BigInteger sum) {
        leaves.put(index, new LeafBranch(hash, sum));
    }

    public MerkleSumTreeRootNode build() {
        List<Branch> branches = new ArrayList<>(leaves.values());

        if (branches.isEmpty()) {
            return new MerkleSumTreeRootNode(new DataHash(algorithm, new byte[32]), BigInteger.ZERO);
        }

        for (int i = 0; i < depth; i++) {
            List<Branch> newBranches = new ArrayList<>();
            for (int j = 0; j < branches.size(); j += 2) {
                Branch left = branches.get(j);
                Branch right = (j + 1 < branches.size()) ? branches.get(j + 1) : new LeafBranch(new DataHash(algorithm, new byte[32]), BigInteger.ZERO);
                newBranches.add(new NodeBranch(left, right, algorithm));
            }
            branches = newBranches;
        }

        return new MerkleSumTreeRootNode(branches.get(0).getHash(), branches.get(0).getSum());
    }
}
