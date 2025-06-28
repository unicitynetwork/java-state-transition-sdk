
package com.unicity.sdk.shared.smst;

import java.util.List;

public class MerkleSumTreePath {
    private final List<MerkleSumTreePathStep> steps;

    public MerkleSumTreePath(List<MerkleSumTreePathStep> steps) {
        this.steps = steps;
    }

    public List<MerkleSumTreePathStep> getSteps() {
        return steps;
    }
}
