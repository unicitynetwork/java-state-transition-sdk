package com.unicity.sdk;

import com.unicity.sdk.shared.smt.MerkleTreePath;
import com.unicity.sdk.shared.smt.MerkleTreePathStep;
import com.unicity.sdk.shared.smt.MerkleTreePathStepBranch;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.util.BitString;
import com.unicity.sdk.shared.util.HexConverter;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class MerklePathDebugTest {
    
    @Test
    void testPathConstruction() {
        // From the test output
        BigInteger firstPath = new BigInteger("3705382724080065743910298682525032525837253764187321552050600457197943668280895");
        String pathBinary = firstPath.toString(2);
        System.out.println("First path: " + firstPath);
        System.out.println("First path hex: " + firstPath.toString(16));
        System.out.println("First path binary: " + pathBinary);
        System.out.println("First path bit length: " + firstPath.bitLength());
        System.out.println("Path binary length: " + pathBinary.length());
        
        // The TypeScript algorithm
        int length = pathBinary.length() - 1;
        System.out.println("\nLength for shifting: " + length);
        
        BigInteger mask = BigInteger.ONE.shiftLeft(length).subtract(BigInteger.ONE);
        System.out.println("Mask: " + mask.toString(16));
        System.out.println("Mask binary: " + mask.toString(2));
        
        BigInteger maskedPath = firstPath.and(mask);
        System.out.println("\nMasked path: " + maskedPath.toString(16));
        
        // Start with currentPath = 1
        BigInteger currentPath = BigInteger.ONE;
        currentPath = currentPath.shiftLeft(length).or(maskedPath);
        System.out.println("\nCurrent path after update: " + currentPath.toString(16));
        
        // Now let's check what the expected pattern should be
        // RequestId: 100002d19405fd4b617269cba8ce30e6f98945d91ab18bcf7ac824b0b24353c7fcfec
        // This is a BitString, so without the leading 1:
        String expectedWithout1 = "00002d19405fd4b617269cba8ce30e6f98945d91ab18bcf7ac824b0b24353c7fcfec";
        System.out.println("\nExpected (without leading 1): " + expectedWithout1);
        
        // The path we computed starts with "2000144..." which is different from expected "00002d19..."
        // This suggests the first step path encoding might be different
    }
    
    @Test
    void testSimpleMerklePath() throws Exception {
        // Let's create a simple test case like in TypeScript
        DataHash root = DataHash.fromImprint(HexConverter.decode("00001fd5fffc41e26f249d04e435b71dbe86d079711131671ed54431a5e117291b42"));
        
        MerkleTreePathStep step1 = new MerkleTreePathStep(
            DataHash.fromImprint(HexConverter.decode("00006c5ad75422175395b4b63390e9dea5d0a39017f4750b78cc4b89ac6451265345")),
            new BigInteger("16"),
            new MerkleTreePathStepBranch(HexConverter.decode("76616c75653030303030303030"))
        );
        
        MerkleTreePathStep step2 = new MerkleTreePathStep(
            DataHash.fromImprint(HexConverter.decode("0000ed454d5723b169c882ec9ad5e7f73b2bb804ec1a3cf1dd0eb24faa833ffd9eef")),
            new BigInteger("4"),
            new MerkleTreePathStepBranch(null)
        );
        
        MerkleTreePathStep step3 = new MerkleTreePathStep(
            DataHash.fromImprint(HexConverter.decode("0000e61c02aab33310b526224da3f2ed765ecea0e9a7ac5a307bf7736cca38d00067")),
            new BigInteger("2"),
            new MerkleTreePathStepBranch(null)
        );
        
        MerkleTreePathStep step4 = new MerkleTreePathStep(
            DataHash.fromImprint(HexConverter.decode("0000be9ef65f6d3b6057acc7668fcbb23f9a5ae573d21bd5ebc3d9f4eee3a3c706a3")),
            new BigInteger("2"),
            new MerkleTreePathStepBranch(null)
        );
        
        MerkleTreePath path = new MerkleTreePath(root, Arrays.asList(step1, step2, step3, step4));
        
        // Test case from TypeScript: 0b100000000n should verify as included
        BigInteger requestId = new BigInteger("256"); // 0b100000000 in decimal
        MerkleTreePath.MerkleTreePathVerificationResult result = path.verify(requestId).get();
        
        System.out.println("\nSimple test result:");
        System.out.println("Path valid: " + result.isPathValid());
        System.out.println("Path included: " + result.isPathIncluded());
        System.out.println("Result: " + result.getResult());
    }
    
    @Test
    void testLargePathValue() {
        // New path value from the latest run
        BigInteger pathFromRun = new BigInteger("926342924890317203487237453657548083316414652360683319662429848360991733441239");
        String requestIdFromRun = "00007abcd58da868d5c078539d519956e3226090fc5b7598666abc7ea1b06cb36ccf";
        
        System.out.println("Path from run: " + pathFromRun);
        System.out.println("Path hex: " + pathFromRun.toString(16));
        System.out.println("Path bits: " + pathFromRun.bitLength());
        
        // Check the RequestId
        String hashOnly = requestIdFromRun.substring(4); // Skip algorithm prefix
        BigInteger requestIdBig = new BigInteger(hashOnly, 16);
        System.out.println("\nRequestId full: " + requestIdFromRun);
        System.out.println("RequestId hash only: " + hashOnly);
        System.out.println("RequestId as BigInteger: " + requestIdBig);
        System.out.println("RequestId hex: " + requestIdBig.toString(16));
        System.out.println("RequestId bits: " + requestIdBig.bitLength());
        
        // Check if they match
        System.out.println("\nDo they match?");
        System.out.println("Path == RequestId: " + pathFromRun.equals(requestIdBig));
        
        // Let's also check what happens if we interpret the path as a BitString
        // In BitString, the leading bit is added to preserve zeros
        // So if this is a BitString representation, we need to check if removing the leading bit gives us the RequestId
        String pathBinary = pathFromRun.toString(2);
        System.out.println("\nPath binary: " + pathBinary);
        System.out.println("Path binary length: " + pathBinary.length());
        
        if (pathBinary.startsWith("1") && pathBinary.length() > 1) {
            String withoutLeading1 = pathBinary.substring(1);
            BigInteger pathWithoutLeading1 = new BigInteger(withoutLeading1, 2);
            System.out.println("\nPath without leading 1: " + pathWithoutLeading1.toString(16));
            System.out.println("Does it match RequestId hash? " + pathWithoutLeading1.equals(requestIdBig));
        }
    }
    
    @Test
    void testPathBuildingLogic() {
        // Let's trace through how the path should be built
        System.out.println("Understanding the sparse merkle tree path algorithm:");
        System.out.println("\nThe tree paths represent navigation through a binary tree.");
        System.out.println("Each step's 'path' value encodes the position in the tree.");
        System.out.println("\nStep paths:");
        
        BigInteger[] paths = {new BigInteger("16"), new BigInteger("4"), new BigInteger("2"), new BigInteger("2")};
        for (int i = 0; i < paths.length; i++) {
            System.out.println("Step " + i + ": path=" + paths[i] + " (binary: " + paths[i].toString(2) + ")");
        }
        
        // Let's think about this differently
        // The RequestId we're looking for is 256 (100000000 in binary)
        // This is a 9-bit value
        
        System.out.println("\nLet's work backwards from the expected RequestId:");
        BigInteger expectedId = new BigInteger("256");
        System.out.println("Expected RequestId: " + expectedId + " (binary: " + expectedId.toString(2) + ")");
        
        // The algorithm builds the path by:
        // 1. Starting with currentPath = 1
        // 2. For each step with a branch, it shifts left and adds bits
        
        System.out.println("\nStep-by-step path construction:");
        BigInteger currentPath = BigInteger.ONE;
        System.out.println("Initial: currentPath = " + currentPath + " (binary: " + currentPath.toString(2) + ")");
        
        // Only the first step has a branch in our test case
        BigInteger path0 = paths[0]; // 16
        String binary0 = path0.toString(2); // "10000"
        int length0 = binary0.length() - 1; // 4
        BigInteger mask0 = BigInteger.ONE.shiftLeft(length0).subtract(BigInteger.ONE); // 15 (1111 in binary)
        BigInteger masked0 = path0.and(mask0); // 16 & 15 = 0
        
        System.out.println("\nStep 0 calculation:");
        System.out.println("  path = " + path0 + " (binary: " + binary0 + ")");
        System.out.println("  length = " + length0);
        System.out.println("  mask = " + mask0 + " (binary: " + mask0.toString(2) + ")");
        System.out.println("  path & mask = " + masked0);
        
        currentPath = currentPath.shiftLeft(length0).or(masked0);
        System.out.println("  currentPath = 1 << 4 | 0 = " + currentPath + " (binary: " + currentPath.toString(2) + ")");
        
        // The issue is that we get 16 (10000) but need 256 (100000000)
        // That's a difference of shifting left by 4 more positions
        
        System.out.println("\n\nAnalysis:");
        System.out.println("We have: " + currentPath + " (binary: " + currentPath.toString(2) + ")");
        System.out.println("We need: " + expectedId + " (binary: " + expectedId.toString(2) + ")");
        System.out.println("\nThe difference suggests we need 4 more bits, which matches the 4 steps.");
        
        // Let me check if there's a pattern with the step paths
        System.out.println("\nStep path analysis:");
        System.out.println("Step 0: 16 = 2^4, indicates level 4");
        System.out.println("Step 1: 4 = 2^2, indicates level 2");
        System.out.println("Step 2: 2 = 2^1, indicates level 1");
        System.out.println("Step 3: 2 = 2^1, indicates level 1");
        System.out.println("\nTotal levels: 4 + 2 + 1 + 1 = 8");
        System.out.println("Expected ID has 9 bits (including the leading 1), which is 8 + 1");
    }
}