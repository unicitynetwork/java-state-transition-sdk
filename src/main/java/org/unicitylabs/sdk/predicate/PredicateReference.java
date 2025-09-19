package org.unicitylabs.sdk.predicate;

import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.hash.DataHash;

public interface PredicateReference {
    DataHash getHash();
    Address toAddress();
}
