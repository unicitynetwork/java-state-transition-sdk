package com.unicity.sdk.transaction;

import com.unicity.sdk.Hashable;
import com.unicity.sdk.hash.DataHash;

public interface TransactionData<T extends Hashable> {
    T getSourceState();
    DataHash getHash();
}
