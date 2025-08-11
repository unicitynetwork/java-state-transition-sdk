package com.unicity.sdk.predicate;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferTransactionData;
import java.io.IOException;

public interface Predicate {
    String getType();
    DataHash calculateHash(TokenId tokenId, TokenType tokenType);
    IPredicateReference getReference(TokenType tokenType);
    byte[] getNonce();
    boolean isOwner(byte[] publicKey);
    boolean verify(Transaction<TransferTransactionData> transaction, TokenId tokenId,
        TokenType tokenType);
}