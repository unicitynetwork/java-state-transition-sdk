package com.unicity.sdk.predicate;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.transaction.Transaction;

public interface IPredicate {
    String getType();
    DataHash getHash();
    DataHash getReference();
    byte[] getNonce();
    boolean isOwner(byte[] publicKey);
    boolean verify(Transaction<?> transaction);
}