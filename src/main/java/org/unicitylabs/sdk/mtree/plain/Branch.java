package org.unicitylabs.sdk.mtree.plain;

import org.unicitylabs.sdk.hash.HashAlgorithm;
import java.math.BigInteger;

interface Branch {
    BigInteger getPath();
    FinalizedBranch finalize(HashAlgorithm hashAlgorithm);
}
