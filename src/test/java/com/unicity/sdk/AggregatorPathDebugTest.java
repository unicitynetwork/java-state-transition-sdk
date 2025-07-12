package com.unicity.sdk;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;

public class AggregatorPathDebugTest {
    
    @Test
    void debugPathValues() {
        // The massive path value from aggregator
        BigInteger aggregatorPath = new BigInteger("1852686833296481835652565514689093443123562524109563101171654987454656221794332");
        System.out.println("Aggregator path (decimal): " + aggregatorPath);
        System.out.println("Aggregator path (hex): " + aggregatorPath.toString(16));
        System.out.println("Aggregator path (binary): " + aggregatorPath.toString(2));
        System.out.println("Aggregator path bit length: " + aggregatorPath.bitLength());
        
        // RequestId from the test
        String requestIdHex = "1000053c13953082598794bdb61f290ca63c3cd63c8fe059421a0e6941dbbcd226403";
        BigInteger requestId = new BigInteger(requestIdHex, 16);
        System.out.println("\nRequestId (hex): " + requestId.toString(16));
        System.out.println("RequestId (binary): " + requestId.toString(2));
        System.out.println("RequestId bit length: " + requestId.bitLength());
        
        // Try to find pattern - maybe aggregator path is shifted or masked version of RequestId
        // Check if lower bits match
        for (int shift = 0; shift < 10; shift++) {
            BigInteger shifted = requestId.shiftRight(shift);
            if (shifted.equals(aggregatorPath)) {
                System.out.println("\nFound match! RequestId >> " + shift + " = aggregatorPath");
            }
        }
        
        // Check if the aggregator path is encoding something specific
        // In sparse merkle trees, paths are usually built by traversing from root
        // The first step path might encode the position in the tree
        
        // Try extracting just the navigation bits
        // Typical SMT uses small integers for navigation (left/right at each level)
        int[] possibleBitLengths = {1, 2, 3, 4, 5, 6, 7, 8};
        for (int bits : possibleBitLengths) {
            BigInteger mask = BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE);
            BigInteger extracted = aggregatorPath.and(mask);
            System.out.println("\nLower " + bits + " bits: " + extracted + " (binary: " + extracted.toString(2) + ")");
        }
        
        // The pattern from the test output shows subsequent paths are small (2, 3, 2, 2...)
        // So the first path is special
        
        // Let's see if there's a relationship with the branch value
        String branchHex = "00007d7dda67d52a27306d0b939b793fd7f7ab722ab836c2cb85b253f43907230291";
        BigInteger branch = new BigInteger(branchHex.substring(4), 16); // Skip algorithm prefix
        System.out.println("\nBranch value (hex): " + branch.toString(16));
        
        // Check XOR relationship
        BigInteger xor = aggregatorPath.xor(requestId);
        System.out.println("\nXOR of aggregatorPath and requestId: " + xor.toString(16));
    }
}