package org.unicitylabs.sdk.serializer.json.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.api.BlockHeightResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
        throw MismatchedInputException.from(p, BlockHeightResponse.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, BlockHeightResponse.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case BLOCK_NUMBER_FIELD:
              if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
                throw MismatchedInputException.from(p, BlockHeightResponse.class,
                    "Expected string value");
              }
              blockNumber = p.readValueAs(Long.class);
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, BlockHeightResponse.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(Set.of(BLOCK_NUMBER_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, BlockHeightResponse.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new BlockHeightResponse(blockNumber);
    }
  }
}
