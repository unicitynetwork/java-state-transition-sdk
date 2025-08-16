package com.unicity.sdk.transaction;

import com.unicity.sdk.address.Address;
import com.unicity.sdk.hash.DataHash;
import java.util.Optional;

public interface TransactionData<T>{
    T getSourceState();
    Address getRecipient();
    Optional<DataHash> getDataHash();
}
