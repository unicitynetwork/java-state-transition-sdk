
package com.unicity.sdk.serializer.transaction;

import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import com.unicity.sdk.transaction.Transaction;

import java.io.IOException;

public class TransactionCborDeserializer {
    public Transaction deserialize(byte[] cbor) {
        CBORFactory factory = new CBORFactory();
        try (CBORParser parser = factory.createParser(cbor)) {
            return parser.readValueAs(Transaction.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
