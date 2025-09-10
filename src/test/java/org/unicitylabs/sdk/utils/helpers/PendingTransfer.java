package com.unicity.sdk.utils.helpers;

import com.unicity.sdk.token.Token;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferTransactionData;

public class PendingTransfer {
    private final Token sourceToken;
    private final Transaction<TransferTransactionData> transaction;

    public PendingTransfer(Token sourceToken, Transaction<TransferTransactionData> transaction) {
        this.sourceToken = sourceToken;
        this.transaction = transaction;
    }

    public Token getSourceToken() { return sourceToken; }
    public Transaction<TransferTransactionData> getTransaction() { return transaction; }
}
