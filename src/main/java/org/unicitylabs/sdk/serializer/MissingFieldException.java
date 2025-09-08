package org.unicitylabs.sdk.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;

public class MissingFieldException extends JsonMappingException {
    public MissingFieldException(JsonParser p, String fieldName) {
        super(p, String.format("Missing required field '%s'", fieldName));
    }

    public static MissingFieldException from(JsonParser p, String field) {
        return new MissingFieldException(p, field);
    }
}
