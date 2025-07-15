package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.smt.path.MerkleTreePath;
import com.unicity.sdk.shared.smt.path.MerkleTreePathVerificationResult;
import com.unicity.sdk.shared.util.HexConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SparseMerkleTreeTest {

    private final MerkleTreeRootNode root = MerkleTreeRootNode.create(
            new PendingNodeBranch(
                    BigInteger.valueOf(0b10L),
                    new PendingNodeBranch(
                            BigInteger.valueOf(0b10L),
                            new PendingNodeBranch(
                                    BigInteger.valueOf(0b100L),
                                    new PendingLeafBranch(
                                            BigInteger.valueOf(0b10000L),
                                            HexConverter.decode("76616c75653030303030303030")
                                    ),
                                    new PendingNodeBranch(
                                            BigInteger.valueOf(0b1001L),
                                            new PendingLeafBranch(
                                                    BigInteger.valueOf(0b10L),
                                                    HexConverter.decode("76616c75653030303130303030")
                                            ),
                                            new PendingLeafBranch(
                                                    BigInteger.valueOf(0b11L),
                                                    HexConverter.decode("76616c75653030303130303030")
                                            )
                                    )
                            ),
                            new PendingLeafBranch(
                                    BigInteger.valueOf(0b11L),
                                    HexConverter.decode("76616c7565313030")
                            )
                    ),
                    new PendingLeafBranch(
                            BigInteger.valueOf(0b1000101L),
                            HexConverter.decode("76616c756530303031303130")
                    )
            ).finalize(HashAlgorithm.SHA256),
            new PendingNodeBranch(
                    BigInteger.valueOf(0b11L),
                    new PendingNodeBranch(
                            BigInteger.valueOf(0b1010L),
                            new PendingLeafBranch(
                                    BigInteger.valueOf(0b11110L),
                                    HexConverter.decode("76616c75653131313030313031")
                            ),
                            new PendingLeafBranch(
                                    BigInteger.valueOf(0b1101L),
                                    HexConverter.decode("76616c756531303130313031")
                            )
                    ),
                    new PendingNodeBranch(
                            BigInteger.valueOf(0b11L),
                            new PendingLeafBranch(
                                    BigInteger.valueOf(0b10L),
                                    HexConverter.decode("76616c7565303131")
                            ),
                            new PendingLeafBranch(
                                    BigInteger.valueOf(0b1111011L),
                                    HexConverter.decode("76616c75653131313031313131")
                            )
                    )
            ).finalize(HashAlgorithm.SHA256),
            HashAlgorithm.SHA256
    );

    public SparseMerkleTreeTest() throws Exception {
    }

    @Test
    public void treeShouldBeHalfCalculated() throws Exception {
        SparseMerkleTree smt = new SparseMerkleTree(HashAlgorithm.SHA256);

        smt.addLeaf(BigInteger.valueOf(0b10L), new byte[]{1, 2, 3});
        smt.calculateRoot();
        smt.addLeaf(BigInteger.valueOf(0b11L), new byte[]{1, 2, 3, 4});

        FinalizedLeafBranch left = new PendingLeafBranch(BigInteger.valueOf(2L), new byte[]{1, 2, 3}).finalize(HashAlgorithm.SHA256);
        PendingLeafBranch right = new PendingLeafBranch(BigInteger.valueOf(3L), new byte[]{1, 2, 3, 4});

        Field leftField = SparseMerkleTree.class.getDeclaredField("left");
        leftField.setAccessible(true);
        Field rightField = SparseMerkleTree.class.getDeclaredField("right");
        rightField.setAccessible(true);

        Assertions.assertEquals(left, leftField.get(smt));
        Assertions.assertEquals(right, rightField.get(smt));
    }

    @Test
    public void shouldVerifyTheTree() throws Exception {
        SparseMerkleTree smt = new SparseMerkleTree(HashAlgorithm.SHA256);
        Map<Long, String> leaves = Map.ofEntries(
                Map.entry(0b110010000L, "value00010000"),
                Map.entry(0b100000000L, "value00000000"),
                Map.entry(0b100010000L, "value00010000"),
                Map.entry(0b111100101L, "value11100101"),
                Map.entry(0b1100L, "value100"),
                Map.entry(0b1011L, "value011"),
                Map.entry(0b111101111L, "value11101111"),
                Map.entry(0b10001010L, "value0001010"),
                Map.entry(0b11010101L, "value1010101")
        );
        for (Map.Entry<Long, String> leaf : leaves.entrySet()) {
            smt.addLeaf(BigInteger.valueOf(leaf.getKey()), leaf.getValue().getBytes(StandardCharsets.UTF_8));
        }

        Assertions.assertThrows(BranchExistsException.class, () ->
                smt.addLeaf(BigInteger.valueOf(0b10000000L), "OnPath".getBytes(StandardCharsets.UTF_8))
        );

        Assertions.assertThrows(LeafOutOfBoundsException.class, () ->
                smt.addLeaf(BigInteger.valueOf(0b1000000000L), "ThroughLeaf".getBytes(StandardCharsets.UTF_8))
        );

        Assertions.assertTrue(smt.calculateRoot().equals(this.root));
    }

    @Test
    public void getPathTest() throws Exception {
        SparseMerkleTree smt = new SparseMerkleTree(HashAlgorithm.SHA256);
        Map<Long, String> leaves = Map.ofEntries(
                Map.entry(0b110010000L, "value00010000"),
                Map.entry(0b100000000L, "value00000000"),
                Map.entry(0b100010000L, "value00010000"),
                Map.entry(0b111100101L, "value11100101"),
                Map.entry(0b1100L, "value100"),
                Map.entry(0b1011L, "value011"),
                Map.entry(0b111101111L, "value11101111"),
                Map.entry(0b10001010L, "value0001010"),
                Map.entry(0b11010101L, "value1010101")
        );
        for (Map.Entry<Long, String> leaf : leaves.entrySet()) {
            smt.addLeaf(BigInteger.valueOf(leaf.getKey()), leaf.getValue().getBytes(StandardCharsets.UTF_8));
        }
        MerkleTreeRootNode root = smt.calculateRoot();

        MerkleTreePath path = root.getPath(BigInteger.valueOf(0b11010L));
        MerkleTreePathVerificationResult result = path.verify(BigInteger.valueOf(0b11010L));
        Assertions.assertFalse(result.isPathIncluded());
        Assertions.assertTrue(result.isPathValid());
        Assertions.assertFalse(result.isValid());

        path = root.getPath(BigInteger.valueOf(0b110010000L));
        result = path.verify(BigInteger.valueOf(0b110010000L));
        Assertions.assertTrue(result.isPathIncluded());
        Assertions.assertTrue(result.isPathValid());
        Assertions.assertTrue(result.isValid());

        path = root.getPath(BigInteger.valueOf(0b110010000L));
        result = path.verify(BigInteger.valueOf(0b11010L));
        Assertions.assertFalse(result.isPathIncluded());
        Assertions.assertTrue(result.isPathValid());
        Assertions.assertFalse(result.isValid());

        path = root.getPath(BigInteger.valueOf(0b10L));
        result = path.verify(BigInteger.valueOf(0b10L));
        Assertions.assertTrue(result.isPathIncluded());
        Assertions.assertTrue(result.isPathValid());
        Assertions.assertTrue(result.isValid());

        SparseMerkleTree emptyTree = new SparseMerkleTree(HashAlgorithm.SHA256);
        MerkleTreeRootNode emptyRoot = emptyTree.calculateRoot();
        path = emptyRoot.getPath(BigInteger.valueOf(0b100L));
        result = path.verify(BigInteger.valueOf(0b10L));
        Assertions.assertFalse(result.isPathIncluded());
        Assertions.assertTrue(result.isPathValid());
        Assertions.assertFalse(result.isValid());
    }
}
