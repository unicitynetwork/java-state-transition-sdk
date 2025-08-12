package com.unicity.sdk.mtree.sum;

import com.unicity.sdk.hash.HashAlgorithm;
import java.math.BigInteger;

interface Branch {
    BigInteger getPath();
    FinalizedBranch finalize(HashAlgorithm hashAlgorithm);
}
