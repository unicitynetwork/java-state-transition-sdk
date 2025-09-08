package org.unicitylabs.sdk.serializer.json.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.unicitylabs.sdk.api.InclusionProofRequest;
import java.io.IOException;

public class InclusionProofRequestJson {

  private static final String REQUEST_ID_FIELD = "requestId";

  private InclusionProofRequestJson() {
  }

  public static class Serializer extends JsonSerializer<InclusionProofRequest> {

    @Override
    public void serialize(InclusionProofRequest value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(REQUEST_ID_FIELD, value.getRequestId());
      gen.writeEndObject();
    }
  }
}
