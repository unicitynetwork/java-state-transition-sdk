package org.unicitylabs.sdk.utils.helpers;

import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferTransactionData;

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
