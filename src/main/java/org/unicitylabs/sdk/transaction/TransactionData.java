package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.hash.DataHash;
import java.util.Optional;

public interface TransactionData<T> {
    T getSourceState();
    Address getRecipient();
    DataHash calculateHash();
    Optional<DataHash> getDataHash();
}
