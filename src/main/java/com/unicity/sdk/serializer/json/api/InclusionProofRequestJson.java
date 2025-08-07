package com.unicity.sdk.serializer.json.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.unicity.sdk.api.InclusionProofRequest;
import com.unicity.sdk.api.SubmitCommitmentRequest;
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
