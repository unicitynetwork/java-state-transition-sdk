package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import org.unicitylabs.sdk.hash.DataHash;

/**
 * Request ID deserializer implementation.
 */
public class RequestIdJson {

  private RequestIdJson() {
  }

  /**
   * Request ID deserializer.
   */
  public static class Deserializer extends StdDeserializer<RequestId> {

    /**
     * Create deserializer.
     */
    public Deserializer() {
      super(RequestId.class);
    }

    /**
     * Deserialize request id.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization
     *            activity.
     * @return request id
     * @throws IOException on deserialization failure
     */
    @Override
    public RequestId deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      return new RequestId(p.readValueAs(DataHash.class));
    }
  }
}
