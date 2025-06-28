
package com.unicity.sdk.serializer.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.transaction.MintTransactionData;

public class MintTransactionJsonDeserializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MintTransactionData deserialize(Object data) {
        return objectMapper.convertValue(data, MintTransactionData.class);
    }
}
