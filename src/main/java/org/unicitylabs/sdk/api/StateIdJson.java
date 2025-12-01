package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import org.unicitylabs.sdk.hash.DataHash;

/**
 * State ID deserializer implementation.
 */
public class StateIdJson {

  private StateIdJson() {
  }

  /**
   * State ID deserializer.
   */
  public static class Deserializer extends StdDeserializer<StateId> {

    /**
     * Create deserializer.
     */
    public Deserializer() {
      super(StateId.class);
    }

    /**
     * Deserialize state id.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization
     *            activity.
     * @return state id
     * @throws IOException on deserialization failure
     */
    @Override
    public StateId deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      return new StateId(p.readValueAs(DataHash.class));
    }
  }
}
