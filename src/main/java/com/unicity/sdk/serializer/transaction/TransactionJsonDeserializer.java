
package com.unicity.sdk.serializer.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.transaction.Transaction;

public class TransactionJsonDeserializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Transaction deserialize(Object data) {
        return objectMapper.convertValue(data, Transaction.class);
    }
}
