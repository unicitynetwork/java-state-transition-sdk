
package com.unicity.sdk.serializer.token;

import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import com.unicity.sdk.token.Token;

import java.io.IOException;

public class TokenCborDeserializer implements ITokenDeserializer {
    @Override
    public Token deserialize(Object data) {
        if (!(data instanceof byte[])) {
            throw new IllegalArgumentException("Data must be a byte array");
        }
        byte[] cbor = (byte[]) data;
        CBORFactory factory = new CBORFactory();
        try (CBORParser parser = factory.createParser(cbor)) {
            return parser.readValueAs(Token.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
