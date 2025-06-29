package com.unicity.sdk.predicate;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.transaction.Transaction;

import java.util.concurrent.CompletableFuture;

public interface IPredicate extends ISerializable {
    DataHash getHash();
    DataHash getReference();
    CompletableFuture<Boolean> isOwner(byte[] publicKey);
    CompletableFuture<Boolean> verify(Transaction<?> transaction);
}