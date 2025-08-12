package com.unicity.sdk.mtree.sum;

import com.unicity.sdk.hash.DataHash;
import java.math.BigInteger;

interface FinalizedBranch extends Branch {
    DataHash getHash();
    BigInteger getCounter();
}
