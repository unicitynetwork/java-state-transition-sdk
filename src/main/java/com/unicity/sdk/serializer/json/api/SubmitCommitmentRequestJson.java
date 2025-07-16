package com.unicity.sdk.serializer.json.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.unicity.sdk.api.SubmitCommitmentRequest;
import java.io.IOException;

public class SubmitCommitmentRequestJson {
  private static final String REQUEST_ID_FIELD = "requestId";
  private static final String TRANSACTION_HASH_FIELD = "transactionHash";
  private static final String AUTHENTICATOR_FIELD = "authenticator";
  private static final String RECEIPT_FIELD = "receipt";

  private SubmitCommitmentRequestJson() {}

  public static class Serializer extends JsonSerializer<SubmitCommitmentRequest> {
    @Override
    public void serialize(SubmitCommitmentRequest value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(REQUEST_ID_FIELD, value.getRequestId());
      gen.writeObjectField(TRANSACTION_HASH_FIELD, value.getTransactionHash());
      gen.writeObjectField(AUTHENTICATOR_FIELD, value.getAuthenticator());
      gen.writeObjectField(RECEIPT_FIELD, value.getReceipt());
      gen.writeEndObject();
    }
  }
}
