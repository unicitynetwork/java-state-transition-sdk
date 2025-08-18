package com.unicity.sdk.serializer.json.token.fungible;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.token.fungible.CoinId;
import com.unicity.sdk.token.fungible.SplitMintReason;
import com.unicity.sdk.token.fungible.SplitMintReasonProof;
import java.io.IOException;
import java.util.Map;

public class SplitMintReasonJson {

  private SplitMintReasonJson() {
  }


  public static class Serializer extends JsonSerializer<SplitMintReason> {
    private static final String TOKEN_FIELD = "token";
    private static final String PROOFS_FIELD = "proofs";


    public Serializer() {
    }

    @Override
    public void serialize(SplitMintReason value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(TOKEN_FIELD, value.getToken());
      gen.writeFieldName(PROOFS_FIELD);
      gen.writeStartArray();
      for (Map.Entry<CoinId, SplitMintReasonProof> entry : value.getProofs().entrySet()) {
        gen.writeStartArray();
        gen.writePOJO(entry.getKey());
        gen.writePOJO(entry.getValue());
        gen.writeEndArray();
      }
      gen.writeEndArray();
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends JsonDeserializer<SplitMintReason> {

    public Deserializer() {
    }

    @Override
    public SplitMintReason deserialize(JsonParser p, DeserializationContext ctx) throws IOException {

      try {
        return null;
      } catch (Exception e) {
        throw MismatchedInputException.from(p, SplitMintReason.class, "Expected bytes");
      }
    }
  }

}
