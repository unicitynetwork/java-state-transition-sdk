package com.unicity.sdk.predicate;

import com.unicity.sdk.address.Address;
import com.unicity.sdk.hash.DataHash;

public interface IPredicateReference {
    DataHash getHash();
    Address toAddress();
}
