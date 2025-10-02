package org.unicitylabs.sdk.mtree.plain;

import java.util.List;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.util.BigIntegerConverter;

public class SparseMerkleTreePathFixture {

  public static SparseMerkleTreePath create(List<SparseMerkleTreePathStep> steps) {
    if (steps.isEmpty()) {
      return new SparseMerkleTreePath(
          new DataHasher(HashAlgorithm.SHA256)
              .update(new byte[]{0})
              .update(new byte[]{0})
              .digest(),
          List.of()
      );
    }

    DataHash root = null;
    for (int i = 0; i < steps.size(); i++) {
      SparseMerkleTreePathStep step = steps.get(i);
      byte[] hash;

      if (step.getBranch().isEmpty()) {
        hash = new byte[]{0};
      } else {
        byte[] bytes = i == 0
            ? step.getBranch().map(SparseMerkleTreePathStep.Branch::getValue).orElse(null)
            : (root != null ? root.getData() : null);

        hash = new DataHasher(HashAlgorithm.SHA256)
            .update(BigIntegerConverter.encode(step.getPath()))
            .update(bytes == null ? new byte[]{0} : bytes)
            .digest()
            .getData();
      }

      byte[] siblingHash = step.getSibling().map(SparseMerkleTreePathStep.Branch::getValue)
          .orElse(new byte[]{0});
      boolean isRight = step.getPath().testBit(0);
      root = new DataHasher(HashAlgorithm.SHA256).update(isRight ? siblingHash : hash)
          .update(isRight ? hash : siblingHash).digest();
    }

    return new SparseMerkleTreePath(root, steps);
  }

}
