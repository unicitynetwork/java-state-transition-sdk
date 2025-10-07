package org.unicitylabs.sdk.utils.helpers;

import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.transaction.TransferTransaction;

public class PendingTransfer {
    private final Token sourceToken;
    private final TransferTransaction transaction;

    public PendingTransfer(Token sourceToken, TransferTransaction transaction) {
        this.sourceToken = sourceToken;
        this.transaction = transaction;
    }

    public Token getSourceToken() { return sourceToken; }
    public TransferTransaction getTransaction() { return transaction; }
}
