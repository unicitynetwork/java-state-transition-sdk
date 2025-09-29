package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.unicitylabs.sdk.hash.DataHash;

/**
 * Deserializer for RequestId objects.
 */
public class RequestIdJsonDeserializer extends JsonDeserializer<RequestId> {

  @Override
  public RequestId deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
    return new RequestId(ctx.readValue(p, DataHash.class));
  }
}
