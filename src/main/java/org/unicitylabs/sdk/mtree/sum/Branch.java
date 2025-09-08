package org.unicitylabs.sdk.mtree.sum;

import org.unicitylabs.sdk.hash.HashAlgorithm;
import java.math.BigInteger;

interface Branch {
    BigInteger getPath();
    FinalizedBranch finalize(HashAlgorithm hashAlgorithm);
}
