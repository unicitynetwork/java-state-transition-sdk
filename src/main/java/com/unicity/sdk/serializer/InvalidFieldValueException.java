package com.unicity.sdk.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;

public class InvalidFieldValueException extends JsonMappingException {
    private InvalidFieldValueException(JsonParser p, String fieldName, Throwable cause) {
        super(p, String.format("Invalid value on field '%s'", fieldName), cause);
    }

    public static InvalidFieldValueException from(JsonParser p, String field) {
        return new InvalidFieldValueException(p, field, null);
    }

    public static InvalidFieldValueException from(JsonParser p, String field, Throwable cause) {
        return new InvalidFieldValueException(p, field, cause);
    }
}
