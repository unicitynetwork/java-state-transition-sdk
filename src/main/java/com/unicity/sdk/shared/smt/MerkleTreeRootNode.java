package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.smt.path.MerkleTreePath;
import com.unicity.sdk.shared.smt.path.MerkleTreePathStep;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MerkleTreeRootNode {
    private final BigInteger path = BigInteger.ONE; // Root path is always 0
    private final FinalizedBranch left;
    private final FinalizedBranch right;
    private final DataHash rootHash;

    private MerkleTreeRootNode(FinalizedBranch left, FinalizedBranch right, DataHash rootHash) {
        this.left = left;
        this.right = right;
        this.rootHash = rootHash;
    }

    public static MerkleTreeRootNode create(FinalizedBranch left, FinalizedBranch right, HashAlgorithm hashAlgorithm) throws Exception {
        DataHash rootHash = new DataHasher(hashAlgorithm).update(left == null ? new byte[]{0} : left.getHash().getData()).update(right == null ? new byte[]{0} : right.getHash().getData()).digest();
        return new MerkleTreeRootNode(left, right, rootHash);
    }

     public DataHash getRootHash() {
         return this.rootHash;
     }

     public MerkleTreePath getPath(BigInteger path) {
         return new MerkleTreePath(this.rootHash, MerkleTreeRootNode.generatePath(path, this.left, this.right));
     }

     @Override
     public boolean equals(Object o) {
         if (!(o instanceof MerkleTreeRootNode)) return false;
         MerkleTreeRootNode that = (MerkleTreeRootNode) o;
         return Objects.equals(path, that.path) && Objects.equals(left, that.left) && Objects.equals(right, that.right) && Objects.equals(rootHash, that.rootHash);
     }

     @Override
     public int hashCode() {
         return Objects.hash(path, left, right, rootHash);
     }

     private static List<MerkleTreePathStep> generatePath(
             BigInteger remainingPath,
             FinalizedBranch left,
             FinalizedBranch right
             )  {
    boolean isRight = remainingPath.testBit(0);
    FinalizedBranch branch = isRight ? right : left;
    FinalizedBranch siblingBranch = isRight ? left : right;

         if (branch == null) {
             return List.of(new MerkleTreePathStep(remainingPath, siblingBranch, (FinalizedLeafBranch) null));
         }

         CommonPath commonPath = CommonPath.create(remainingPath, branch.getPath());
         if (branch.getPath().equals(commonPath.getPath())) {
             if (branch instanceof FinalizedLeafBranch) {
                 return List.of(new MerkleTreePathStep(branch.getPath(), siblingBranch, (FinalizedLeafBranch) branch));
             }

             FinalizedNodeBranch nodeBranch = (FinalizedNodeBranch) branch;

             if (remainingPath.shiftRight(commonPath.getLength()).compareTo(BigInteger.ONE) == 0) {
                 return List.of(new MerkleTreePathStep(branch.getPath(), siblingBranch, nodeBranch));
             }

             return Stream.concat(
                     MerkleTreeRootNode.generatePath(remainingPath.shiftRight(commonPath.getLength()), nodeBranch.getLeft(), nodeBranch.getRight()).stream(),
                     Stream.of(new MerkleTreePathStep(branch.getPath(), siblingBranch, nodeBranch))
             ).collect(Collectors.toUnmodifiableList());
         }

         if (branch instanceof FinalizedLeafBranch) {
             return List.of(new MerkleTreePathStep(branch.getPath(), siblingBranch, (FinalizedLeafBranch) branch));
         }

         return List.of(new MerkleTreePathStep(branch.getPath(), siblingBranch, (FinalizedNodeBranch) branch));
     }
 }
