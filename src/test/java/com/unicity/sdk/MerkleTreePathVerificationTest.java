package com.unicity.sdk;

import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.smt.MerkleTreePath;
import com.unicity.sdk.shared.smt.MerkleTreePathStep;
import com.unicity.sdk.shared.smt.MerkleTreePathStepBranch;
import com.unicity.sdk.shared.util.BitString;
import com.unicity.sdk.shared.util.HexConverter;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MerkleTreePathVerificationTest {
    
    @Test
    void testSimpleMerkleTreePath() throws Exception {
        // Create a simple test case
        String rootHex = "0000b955b9b3b40b674ad89f2bcedb76db8c4abb5d4349e0fca398e773fb6a4568bb";
        DataHash root = DataHash.fromImprint(HexConverter.decode(rootHex));
        
        // Create a single step
        List<MerkleTreePathStep> steps = new ArrayList<>();
        
        // First step with branch value
        BigInteger path1 = new BigInteger("7");
        DataHash sibling1 = DataHash.fromImprint(HexConverter.decode("0000e735de81edcff75c79451e39be359a35834e365aeb562fc2857da0e4b4713d93"));
        byte[] branchValue = HexConverter.decode("00006726c078b79d3710049123dfaa46e8ea1b23ffc6bec99a6919ec22c1f0b7d60a");
        MerkleTreePathStepBranch branch1 = new MerkleTreePathStepBranch(branchValue);
        
        MerkleTreePathStep step1 = new MerkleTreePathStep(sibling1, path1, branch1);
        steps.add(step1);
        
        // Create merkle tree path
        MerkleTreePath merkleTreePath = new MerkleTreePath(root, steps);
        
        // Create a request ID to verify
        String requestIdHex = "00008749337bb4113f53465909c757a229588ef6193580a00fc54a22210d84624de2";
        DataHash requestIdHash = DataHash.fromImprint(HexConverter.decode(requestIdHex));
        
        // Convert to BitString and then to BigInteger
        BitString bitString = BitString.fromDataHash(requestIdHash);
        BigInteger requestIdBigInt = bitString.toBigInteger();
        
        System.out.println("RequestId hex: " + requestIdHex);
        System.out.println("RequestId as BigInteger: " + requestIdBigInt.toString(16));
        System.out.println("RequestId as BitString bits: " + requestIdBigInt.bitLength());
        
        // Verify
        MerkleTreePath.MerkleTreePathVerificationResult result = merkleTreePath.verify(requestIdBigInt).get();
        
        System.out.println("\nVerification result:");
        System.out.println("Path valid: " + result.isPathValid());
        System.out.println("Path included: " + result.isPathIncluded());
        System.out.println("Overall result: " + result.getResult());
    }
}