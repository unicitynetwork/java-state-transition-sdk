package com.unicity.sdk.smt;

import com.unicity.sdk.hash.HashAlgorithm;

import java.math.BigInteger;
import java.util.*;

public class SparseMerkleTree {
    private Branch left = null;
    private Branch right = null;

    private final HashAlgorithm hashAlgorithm;

    public SparseMerkleTree(HashAlgorithm hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public synchronized void addLeaf(BigInteger path, byte[] data) throws Exception {
        if (path.compareTo(BigInteger.ONE) < 0) {
            throw new IllegalArgumentException("Path must be greater than 0");
        }

        boolean isRight = path.testBit(0);
        Branch branch = isRight ? right : left;
        Branch result = branch != null
                ? SparseMerkleTree.buildTree(branch, path, Arrays.copyOf(data, data.length))
                : new PendingLeafBranch(path, Arrays.copyOf(data, data.length));

        if (isRight) {
            this.right = result;
        } else {
            this.left = result;
        }
    }

    public synchronized MerkleTreeRootNode calculateRoot() {
        FinalizedBranch left = this.left != null ? this.left.finalize(this.hashAlgorithm) : null;
        FinalizedBranch right = this.right != null ? this.right.finalize(this.hashAlgorithm) : null;
        this.left = left;
        this.right = right;

        return MerkleTreeRootNode.create(left, right, this.hashAlgorithm);
    }

    private static Branch buildTree(Branch branch, BigInteger remainingPath, byte[] value) throws BranchExistsException, LeafOutOfBoundsException {
        CommonPath commonPath = CommonPath.create(remainingPath, branch.getPath());
        boolean isRight = remainingPath.shiftRight(commonPath.getLength()).testBit(0);

        if (commonPath.getPath().equals(remainingPath)) {
            throw new BranchExistsException();
        }

        if (branch instanceof LeafBranch) {
            if (commonPath.getPath().equals(branch.getPath())) {
                throw new LeafOutOfBoundsException();
            }

            LeafBranch leafBranch = (LeafBranch) branch;

            LeafBranch oldBranch = new PendingLeafBranch(branch.getPath().shiftRight(commonPath.getLength()), leafBranch.getValue());
            LeafBranch newBranch = new PendingLeafBranch(remainingPath.shiftRight(commonPath.getLength()), value);
            return new PendingNodeBranch(commonPath.getPath(), isRight ? oldBranch : newBranch, isRight ? newBranch : oldBranch);
        }

        NodeBranch nodeBranch = (NodeBranch) branch;

        // if node branch is split in the middle
        if (commonPath.getPath().compareTo(branch.getPath()) < 0) {
            LeafBranch newBranch = new PendingLeafBranch(remainingPath.shiftRight(commonPath.getLength()), value);
            NodeBranch oldBranch = new PendingNodeBranch(branch.getPath().shiftRight(commonPath.getLength()), nodeBranch.getLeft(), nodeBranch.getRight());
            return new PendingNodeBranch(commonPath.getPath(), isRight ? oldBranch : newBranch, isRight ? newBranch : oldBranch);
        }

        if (isRight) {
            return new PendingNodeBranch(nodeBranch.getPath(), nodeBranch.getLeft(), SparseMerkleTree.buildTree(nodeBranch.getRight(), remainingPath.shiftRight(commonPath.getLength()), value));
        }

        return new PendingNodeBranch(nodeBranch.getPath(), SparseMerkleTree.buildTree(nodeBranch.getLeft(), remainingPath.shiftRight(commonPath.getLength()), value), nodeBranch.getRight());
    }

    @Override
    public String toString() {
        return "Left: " + (left != null ? left.toString() : "null") + ", Right: " + (right != null ? right.toString() : "null");
    }
}

