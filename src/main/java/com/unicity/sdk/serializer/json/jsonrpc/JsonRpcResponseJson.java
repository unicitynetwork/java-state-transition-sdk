package com.unicity.sdk.serializer.json.jsonrpc;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.jsonrpc.JsonRpcError;
import com.unicity.sdk.jsonrpc.JsonRpcResponse;
import com.unicity.sdk.smt.path.MerkleTreePath;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class JsonRpcResponseJson {

  private static final String VERSION_FIELD = "jsonrpc";
  private static final String RESULT_FIELD = "result";
  private static final String ERROR_FIELD = "error";
  private static final String ID_FIELD = "id";

  private JsonRpcResponseJson() {
  }

  public static class Deserializer extends JsonDeserializer<JsonRpcResponse<?>> implements
      ContextualDeserializer {

    private final JavaType resultType;

    public Deserializer() {
      this.resultType = null;
    }

    private Deserializer(JavaType valueType) {
      this.resultType = valueType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
      JavaType wrapperType = ctxt.getContextualType();
      JavaType valueType = wrapperType != null ? wrapperType.containedType(0) : null;
      return new Deserializer(valueType);
    }

    @Override
    public JsonRpcResponse<?> deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      String version = null;
      Object result = null;
      JsonRpcError error = null;
      UUID id = null;

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, JsonRpcResponse.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, JsonRpcResponse.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case VERSION_FIELD:
              if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
                throw MismatchedInputException.from(p, JsonRpcResponse.class,
                    "Expected string value");
              }
              version = p.readValueAs(String.class);
              break;
            case RESULT_FIELD:
              result = p.getCodec().readValue(p, this.resultType);
              break;
            case ERROR_FIELD:
              error = p.readValueAs(JsonRpcError.class);
              break;
            case ID_FIELD:
              if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
                throw MismatchedInputException.from(p, JsonRpcResponse.class,
                    "Expected string value");
              }
              id = p.readValueAs(UUID.class);
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, JsonRpcResponse.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(Set.of(VERSION_FIELD, ID_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, MerkleTreePath.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new JsonRpcResponse<>(version, result, error, id);
    }
  }
}
