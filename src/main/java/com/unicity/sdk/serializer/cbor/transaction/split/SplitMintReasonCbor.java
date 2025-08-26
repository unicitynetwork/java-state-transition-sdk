package com.unicity.sdk.serializer.cbor.transaction.split;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.transaction.split.SplitMintReason;
import com.unicity.sdk.transaction.split.SplitMintReasonProof;
import java.io.IOException;
import java.util.List;

public class SplitMintReasonCbor {
  private SplitMintReasonCbor() {
  }


  public static class Serializer extends JsonSerializer<SplitMintReason> {
    public Serializer() {
    }

    @Override
    public void serialize(SplitMintReason value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 2);
      gen.writeObject(value.getToken());
      gen.writeObject(value.getProofs());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<SplitMintReason> {

    public Deserializer() {
    }

    @Override
    public SplitMintReason deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, SplitMintReason.class, "Expected array value");
      }

      return new SplitMintReason(
          p.readValueAs(Token.class),
          ctx.readValue(p, ctx.getTypeFactory().constructCollectionType(List.class, SplitMintReasonProof.class))
      );
    }
  }

}
