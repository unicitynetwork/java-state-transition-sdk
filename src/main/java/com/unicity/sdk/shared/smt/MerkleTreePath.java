
package com.unicity.sdk.shared.smt;

import java.util.List;

public class MerkleTreePath {
    private final List<MerkleTreePathStep> steps;

    public MerkleTreePath(List<MerkleTreePathStep> steps) {
        this.steps = steps;
    }

    public List<MerkleTreePathStep> getSteps() {
        return steps;
    }
}
