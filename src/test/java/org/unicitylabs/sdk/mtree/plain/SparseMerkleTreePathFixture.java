package org.unicitylabs.sdk.mtree.plain;

import java.util.List;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.util.BigIntegerConverter;

public class SparseMerkleTreePathFixture {

  public static SparseMerkleTreePath create() {
    return new SparseMerkleTreePath(
        new DataHasher(HashAlgorithm.SHA256)
            .update(new byte[]{0})
            .update(new byte[]{0})
            .digest(),
        List.of()
    );
  }

}
