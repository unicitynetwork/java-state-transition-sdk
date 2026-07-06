package org.unicitylabs.sdk.smt.radix;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.api.InclusionCertificate;
import org.unicitylabs.sdk.api.StateId;
import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;

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

  @Test
  void deepSplitAtDepth255VerifiesWithRegion() throws Exception {
    SparseMerkleTree tree = new SparseMerkleTree(HashAlgorithm.SHA256);

    byte[] a = new byte[32];
    byte[] b = new byte[32];
    b[31] = (byte) 0x80;
    byte[] valueA = new byte[32];
    valueA[0] = 1;
    byte[] valueB = new byte[32];
    valueB[0] = 2;

    tree.addLeaf(a, valueA);
    tree.addLeaf(b, valueB);
    FinalizedNodeBranch root = tree.calculateRoot();

    for (byte[][] entry : new byte[][][]{{a, valueA}, {b, valueB}}) {
      InclusionCertificate certificate = InclusionCertificate.create(root, entry[0]);
      StateId key = StateId.fromCbor(CborSerializer.encodeByteString(entry[0]));
      DataHash value = new DataHash(HashAlgorithm.SHA256, entry[1]);
      Assertions.assertTrue(certificate.verify(key, value, root.getHash()));
    }
  }
}
