package com.unicity.sdk.mtree.plain;

import com.unicity.sdk.hash.HashAlgorithm;
import java.math.BigInteger;

interface Branch {
    BigInteger getPath();
    FinalizedBranch finalize(HashAlgorithm hashAlgorithm);
}
