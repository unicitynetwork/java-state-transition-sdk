package com.unicity.sdk.predicate;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.identity.IIdentity;

public interface IPredicate extends ISerializable {
    DataHash getHash();
    IIdentity getReference();
}