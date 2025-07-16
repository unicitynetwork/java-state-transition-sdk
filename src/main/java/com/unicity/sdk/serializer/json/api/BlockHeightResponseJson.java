package com.unicity.sdk.serializer.json.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.api.BlockHeightResponse;
import com.unicity.sdk.jsonrpc.JsonRpcError;
import com.unicity.sdk.jsonrpc.JsonRpcResponse;
import com.unicity.sdk.smt.path.MerkleTreePath;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BlockHeightResponseJson {

  private static final String BLOCK_NUMBER_FIELD = "blockNumber";

  private BlockHeightResponseJson() {
  }

  public static class Deserializer extends JsonDeserializer<BlockHeightResponse>{

    public Deserializer() {
    }


    @Override
    public BlockHeightResponse deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      Long blockNumber = null;

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
            case BLOCK_NUMBER_FIELD:
              if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
                throw MismatchedInputException.from(p, JsonRpcResponse.class,
                    "Expected string value");
              }
              blockNumber = p.getValueAsLong();
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, JsonRpcResponse.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(Set.of(BLOCK_NUMBER_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, MerkleTreePath.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new BlockHeightResponse(blockNumber);
    }
  }
}
