package org.unicitylabs.sdk.serializer.cbor.bft;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.unicitylabs.sdk.bft.UnicitySeal;

public class UnicitySealCbor {

  private UnicitySealCbor() {
  }

  public static class Serializer extends JsonSerializer<UnicitySeal> {

    @Override
    public void serialize(UnicitySeal value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 0);
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<UnicitySeal> {

    @Override
    public UnicitySeal deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, UnicitySeal.class, "Expected array value");
      }
      p.nextToken();

      BigInteger version = p.readValueAs(BigInteger.class);
      BigInteger networkId = p.readValueAs(BigInteger.class);
      BigInteger rootChainRoundNumber = p.readValueAs(BigInteger.class);
      BigInteger epoch = p.readValueAs(BigInteger.class);
      BigInteger timestamp = p.readValueAs(BigInteger.class);
      byte[] previousHash = p.readValueAs(byte[].class);
      byte[] hash = p.readValueAs(byte[].class);

      if (p.nextToken() != JsonToken.START_OBJECT) {
        throw MismatchedInputException.from(p, UnicitySeal.class, "Expected map value");
      }

      Map<String, byte[]> signatures = new HashMap<>();
      while (p.nextToken() != JsonToken.END_OBJECT) {
        String name = p.currentName();
        p.nextToken();
        signatures.put(name, p.readValueAs(byte[].class));
      }

      return new UnicitySeal(
          version,
          networkId,
          rootChainRoundNumber,
          epoch,
          timestamp,
          previousHash,
          hash,
          signatures
      );
    }
  }
}
