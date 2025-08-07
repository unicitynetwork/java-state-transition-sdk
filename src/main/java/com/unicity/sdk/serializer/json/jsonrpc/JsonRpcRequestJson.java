package com.unicity.sdk.serializer.json.jsonrpc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.unicity.sdk.jsonrpc.JsonRpcRequest;
import java.io.IOException;

public class JsonRpcRequestJson {

  private static final String ID_FIELD = "id";
  private static final String VERSION_FIELD = "jsonrpc";
  private static final String METHOD_FIELD = "method";
  private static final String PARAMS_FIELD = "params";

  private JsonRpcRequestJson() {
  }

  public static class Serializer extends JsonSerializer<JsonRpcRequest> {

    @Override
    public void serialize(JsonRpcRequest value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(ID_FIELD, value.getId());
      gen.writeObjectField(VERSION_FIELD, value.getVersion());
      gen.writeObjectField(METHOD_FIELD, value.getMethod());
      gen.writeObjectField(PARAMS_FIELD, value.getParams());
      gen.writeEndObject();
    }
  }
}
