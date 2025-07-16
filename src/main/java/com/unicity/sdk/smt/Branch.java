package com.unicity.sdk.smt;

import com.unicity.sdk.hash.HashAlgorithm;

import java.math.BigInteger;

public interface Branch {
    BigInteger getPath();
    FinalizedBranch finalize(HashAlgorithm hashAlgorithm);
}
