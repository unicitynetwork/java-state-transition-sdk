package com.unicity.sdk.serializer.json.jsonrpc;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.jsonrpc.JsonRpcError;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class JsonRpcErrorJson {

  private static final String CODE_FIELD = "code";
  private static final String MESSAGE_FIELD = "message";

  private JsonRpcErrorJson() {
  }

  public static class Deserializer extends JsonDeserializer<JsonRpcError> {


    public Deserializer() {
    }

    @Override
    public JsonRpcError deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      Integer code = null;
      String message = null;

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, JsonRpcError.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, JsonRpcError.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();

        try {
          switch (fieldName) {
            case CODE_FIELD:
              // TODO: Check field type?
              code = p.readValueAs(Integer.class);
              break;
            case MESSAGE_FIELD:
              if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
                throw MismatchedInputException.from(p, JsonRpcError.class,
                    "Expected string value");
              }
              message = p.readValueAs(String.class);
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, JsonRpcError.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(Set.of(CODE_FIELD, MESSAGE_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, JsonRpcError.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new JsonRpcError(code, message);
    }
  }
}
