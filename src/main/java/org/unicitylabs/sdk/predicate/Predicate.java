package org.unicitylabs.sdk.predicate;

import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferTransaction;

public interface Predicate extends SerializablePredicate {
  DataHash calculateHash();

  PredicateReference getReference();

  boolean isOwner(byte[] publicKey);

  boolean verify(Token<?> token, TransferTransaction transaction, RootTrustBase trustBase);
}

