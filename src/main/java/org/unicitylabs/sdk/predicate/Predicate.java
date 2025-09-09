package org.unicitylabs.sdk.predicate;

import java.util.List;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferTransactionData;

public interface Predicate {
    String getType();
    DataHash calculateHash(TokenId tokenId, TokenType tokenType);
    IPredicateReference getReference(TokenType tokenType);
    byte[] getNonce();
    boolean isOwner(byte[] publicKey);
    boolean verify(
        List<Transaction<TransferTransactionData>> transactions,
        Token<?> token
    );
}