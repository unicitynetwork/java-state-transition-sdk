package com.unicity.sdk.serializer.hash;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.util.HexConverter;

import java.io.IOException;

public class DataHashSerializer extends JsonSerializer<DataHash> {
    @Override
    public void serialize(DataHash value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(HexConverter.encode(value.getImprint()));
    }
}
