package com.unicity.sdk.transaction;

import com.unicity.sdk.address.Address;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;

public interface TransactionData<T>{
    T getSourceState();
    Address getRecipient();
}
