
package com.unicity.sdk.shared.smst;

import com.unicity.sdk.shared.hash.DataHash;

import java.math.BigInteger;

public interface PendingBranch {
    DataHash getHash();
    BigInteger getSum();
}
