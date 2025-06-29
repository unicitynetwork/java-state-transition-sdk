package com.unicity.sdk.shared.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * CBOR encoder using Jackson's CBOR library
 */
public class JacksonCborEncoder {
    
    private static final ObjectMapper CBOR_MAPPER = new ObjectMapper(new CBORFactory()
            .configure(CBORGenerator.Feature.WRITE_MINIMAL_INTS, true));
    
    /**
     * Encode an array of CBOR values
     */
    public static byte[] encodeArray(Object... values) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            JsonGenerator gen = CBOR_MAPPER.getFactory().createGenerator(baos);
            gen.writeStartArray();
            
            for (Object value : values) {
                writeValue(gen, value);
            }
            
            gen.writeEndArray();
            gen.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode CBOR array", e);
        }
    }
    
    /**
     * Encode a list as CBOR array
     */
    public static byte[] encodeList(List<?> values) {
        return encodeArray(values.toArray());
    }
    
    /**
     * Encode a map as CBOR map
     */
    public static byte[] encodeMap(Map<?, ?> map) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            JsonGenerator gen = CBOR_MAPPER.getFactory().createGenerator(baos);
            gen.writeStartObject();
            
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                // Key must be a string in CBOR maps for compatibility
                gen.writeFieldName(entry.getKey().toString());
                writeValue(gen, entry.getValue());
            }
            
            gen.writeEndObject();
            gen.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode CBOR map", e);
        }
    }
    
    /**
     * Encode a single value as CBOR
     */
    public static byte[] encodeValue(Object value) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            JsonGenerator gen = CBOR_MAPPER.getFactory().createGenerator(baos);
            writeValue(gen, value);
            gen.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode CBOR value", e);
        }
    }
    
    /**
     * Write a value to the CBOR generator
     */
    private static void writeValue(JsonGenerator gen, Object value) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else if (value instanceof byte[]) {
            gen.writeBinary((byte[]) value);
        } else if (value instanceof String) {
            gen.writeString((String) value);
        } else if (value instanceof Boolean) {
            gen.writeBoolean((Boolean) value);
        } else if (value instanceof Integer) {
            gen.writeNumber((Integer) value);
        } else if (value instanceof Long) {
            gen.writeNumber((Long) value);
        } else if (value instanceof BigInteger) {
            // Encode BigInteger as byte string for compatibility with TypeScript
            gen.writeBinary(((BigInteger) value).toByteArray());
        } else if (value instanceof Object[]) {
            gen.writeStartArray();
            for (Object item : (Object[]) value) {
                writeValue(gen, item);
            }
            gen.writeEndArray();
        } else if (value instanceof List) {
            gen.writeStartArray();
            for (Object item : (List<?>) value) {
                writeValue(gen, item);
            }
            gen.writeEndArray();
        } else if (value instanceof Map) {
            gen.writeStartObject();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                gen.writeFieldName(entry.getKey().toString());
                writeValue(gen, entry.getValue());
            }
            gen.writeEndObject();
        } else {
            throw new IllegalArgumentException("Unsupported type for CBOR encoding: " + value.getClass());
        }
    }
}