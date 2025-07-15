
package com.unicity.sdk.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.unicity.sdk.shared.util.HexConverter;

import java.io.IOException;

public class ByteArraySerializer extends JsonSerializer<byte[]> {
    @Override
    public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(HexConverter.encode(value));
    }
}
