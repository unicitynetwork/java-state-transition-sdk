package org.unicitylabs.sdk.smt.radixsum;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.payment.SplitAllocationProof;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * Radix sparse Merkle sum tree tests, mirroring the JS SDK suite.
 */
public class SparseMerkleSumTreeTest {

  private static byte[] key(int... bytes) {
    byte[] key = new byte[32];
    for (int i = 0; i < bytes.length; i++) {
      key[i] = (byte) bytes[i];
    }
    return key;
  }

  private static byte[] data(int seed) {
    byte[] data = new byte[32];
    Arrays.fill(data, (byte) seed);
    return data;
  }

  private static final List<Leaf> LEAVES = List.of(
          new Leaf(data(1), key(0b10010000), BigInteger.valueOf(5)),
          new Leaf(data(2), key(0b00000000), BigInteger.valueOf(10)),
          new Leaf(data(3), key(0b00010000), BigInteger.valueOf(20)),
          new Leaf(data(4), key(0b10000000), BigInteger.valueOf(40)),
          new Leaf(data(5), key(0b01100000), BigInteger.valueOf(80)),
          new Leaf(data(6), key(0b00010100), BigInteger.valueOf(160))
  );

  private static SparseMerkleSumTree build(List<Leaf> leaves) throws Exception {
    SparseMerkleSumTree tree = new SparseMerkleSumTree(HashAlgorithm.SHA256);
    for (Leaf leaf : leaves) {
      tree.addLeaf(leaf.key, leaf.data, leaf.value);
    }
    return tree;
  }

  @Test
  public void reconstructsRootSumAndVerifiesEveryLeaf() throws Exception {
    SparseMerkleSumTreeRootNode root = build(LEAVES).calculateRoot();
    BigInteger total = LEAVES.stream().map(leaf -> leaf.value)
            .reduce(BigInteger.ZERO, BigInteger::add);
    Assertions.assertEquals(total, root.getValue());

    for (Leaf leaf : LEAVES) {
      SplitAllocationProof proof = SplitAllocationProof.create(root, leaf.key);
      Assertions.assertEquals(total,
              proof.calculateRoot(leaf.key, leaf.data, leaf.value).getSum());
      Assertions.assertTrue(proof.verify(leaf.key, leaf.data, leaf.value, root.getHash(), total));
    }
  }

  @Test
  public void rejectsTamperedLeafAmount() throws Exception {
    SparseMerkleSumTreeRootNode root = build(LEAVES).calculateRoot();
    Leaf leaf = LEAVES.get(0);
    SplitAllocationProof proof = SplitAllocationProof.create(root, leaf.key);
    Assertions.assertFalse(proof.verify(
            leaf.key, leaf.data, leaf.value.add(BigInteger.ONE), root.getHash(), leaf.value));
  }

  @Test
  public void producesEmptyProofForSingleLeafTree() throws Exception {
    Leaf leaf = LEAVES.get(0);
    SparseMerkleSumTreeRootNode root = build(List.of(leaf)).calculateRoot();
    SplitAllocationProof proof = SplitAllocationProof.create(root, leaf.key);
    Assertions.assertEquals(0, proof.getLength());
    Assertions.assertEquals(leaf.value, root.getValue());

    Assertions.assertEquals(leaf.value,
            proof.calculateRoot(leaf.key, leaf.data, leaf.value).getSum());
    Assertions.assertTrue(
            proof.verify(leaf.key, leaf.data, leaf.value, root.getHash(), leaf.value));
  }

  @Test
  public void survivesCborRoundTripOfInclusionProof() throws Exception {
    SparseMerkleSumTreeRootNode root = build(LEAVES).calculateRoot();
    BigInteger total = LEAVES.stream().map(leaf -> leaf.value)
            .reduce(BigInteger.ZERO, BigInteger::add);
    Leaf leaf = LEAVES.get(4);
    SplitAllocationProof proof = SplitAllocationProof.create(root, leaf.key);
    SplitAllocationProof decoded = SplitAllocationProof.fromCbor(proof.toCbor());
    Assertions.assertTrue(decoded.verify(leaf.key, leaf.data, leaf.value, root.getHash(), total));
  }

  private static final class Leaf {
    private final byte[] data;
    private final byte[] key;
    private final BigInteger value;

    private Leaf(byte[] data, byte[] key, BigInteger value) {
      this.data = data;
      this.key = key;
      this.value = value;
    }
  }
}
