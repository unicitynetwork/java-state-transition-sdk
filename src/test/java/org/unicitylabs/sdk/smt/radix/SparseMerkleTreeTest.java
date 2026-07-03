package org.unicitylabs.sdk.smt.radix;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;

public class SparseMerkleTreeTest {

  @Test
  void addLeafRejectsNon32ByteKeyOrData() {
    SparseMerkleTree tree = new SparseMerkleTree(HashAlgorithm.SHA256);
    Assertions.assertThrows(IllegalArgumentException.class,
            () -> tree.addLeaf(new byte[31], new byte[32]));
    Assertions.assertThrows(IllegalArgumentException.class,
            () -> tree.addLeaf(new byte[32], new byte[31]));
    Assertions.assertThrows(NullPointerException.class,
            () -> tree.addLeaf(null, new byte[32]));
  }
}
