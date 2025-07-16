package com.unicity.sdk.predicate;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.transaction.Transaction;

public interface IPredicate {
    String getType();
    DataHash getHash();
    IPredicateReference getReference();
    byte[] getNonce();
    boolean isOwner(byte[] publicKey);
    boolean verify(Transaction<?> transaction);
}