package org.unicitylabs.sdk.mtree.sum;

import org.unicitylabs.sdk.hash.DataHash;
import java.math.BigInteger;

interface FinalizedBranch extends Branch {
    DataHash getHash();
    BigInteger getCounter();
}
