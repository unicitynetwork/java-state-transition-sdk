
package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class SparseMerkleTreePathUtils {
    public static PathVerificationResult verify(
            MerkleTreePath path,
            DataHash rootHash,
            BigInteger index,
            DataHash leafHash,
            HashAlgorithm algorithm
    ) {
        DataHash currentHash = leafHash;
        BigInteger currentIndex = index;

        for (MerkleTreePathStep step : path.getSteps()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                if (currentIndex.testBit(0)) { // if right
                    baos.write(step.getHash().getHash());
                    baos.write(currentHash.getHash());
                } else { // if left
                    baos.write(currentHash.getHash());
                    baos.write(step.getHash().getHash());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            currentHash = DataHasher.digest(algorithm, baos.toByteArray());
            currentIndex = currentIndex.shiftRight(1);
        }

        if (currentHash.equals(rootHash)) {
            return PathVerificationResult.OK;
        } else {
            return PathVerificationResult.PATH_NOT_INCLUDED;
        }
    }
}
