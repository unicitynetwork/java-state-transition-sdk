package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.math.BigInteger;

public interface Branch {
    BigInteger getPath();
    FinalizedBranch finalize(HashAlgorithm hashAlgorithm) throws Exception;
}
