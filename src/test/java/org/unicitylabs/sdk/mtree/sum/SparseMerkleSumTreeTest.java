package org.unicitylabs.sdk.mtree.sum;


import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTree.LeafValue;
import java.math.BigInteger;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SparseMerkleSumTreeTest {

  @Test
  void shouldBuildTreeWithNumericValues() throws Exception {
    var leaves = Map.of(
        new BigInteger("1000", 2), new LeafValue("left-1".getBytes(), BigInteger.valueOf(10)),
        new BigInteger("1001", 2), new LeafValue("right-1".getBytes(), BigInteger.valueOf(20)),
        new BigInteger("1010", 2), new LeafValue("left-2".getBytes(), BigInteger.valueOf(30)),
        new BigInteger("1011", 2), new LeafValue("right-2".getBytes(), BigInteger.valueOf(40))
    );

    SparseMerkleSumTree tree = new SparseMerkleSumTree(HashAlgorithm.SHA256);
    for (Entry<BigInteger, LeafValue> entry : leaves.entrySet()) {
      tree.addLeaf(entry.getKey(), entry.getValue());
    }

    var root = tree.calculateRoot();
    Assertions.assertEquals(BigInteger.valueOf(100), root.getValue());

    for (var entry : leaves.entrySet()) {
      var path = root.getPath(entry.getKey());
      var verificationResult = path.verify(entry.getKey());
      Assertions.assertTrue(verificationResult.isPathIncluded());
      Assertions.assertTrue(verificationResult.isPathValid());
      Assertions.assertTrue(verificationResult.isSuccessful());

      Assertions.assertEquals(root.getRootHash(), path.getRootHash());
      Assertions.assertArrayEquals(
          entry.getValue().getValue(),
          path.getSteps().get(0).getData().orElse(null)
      );
      Assertions.assertEquals(
          entry.getValue().getCounter(),
          path.getSteps().get(0).getValue()
      );
    }

    tree.addLeaf(new BigInteger("1110", 2), new LeafValue(new byte[32], BigInteger.valueOf(100)));
    root = tree.calculateRoot();
    Assertions.assertEquals(BigInteger.valueOf(200), root.getValue());
  }

  @Test
  void shouldThrowErrorOnNonPositivePathOrSum() {
    var tree = new SparseMerkleSumTree(HashAlgorithm.SHA256);
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> tree.addLeaf(BigInteger.valueOf(-1),
            new LeafValue(new byte[32], BigInteger.valueOf(100))));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> tree.addLeaf(BigInteger.ONE, new LeafValue(new byte[32], BigInteger.valueOf(-1))));
  }
}
