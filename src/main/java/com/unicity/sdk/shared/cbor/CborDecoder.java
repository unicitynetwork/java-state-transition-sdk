
package com.unicity.sdk.shared.cbor;

import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;

import java.io.IOException;

public class CborDecoder {
    public static <T> T decode(byte[] cbor, Class<T> valueType) {
        CBORFactory factory = new CBORFactory();
        try (CBORParser parser = factory.createParser(cbor)) {
            return parser.readValueAs(valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
