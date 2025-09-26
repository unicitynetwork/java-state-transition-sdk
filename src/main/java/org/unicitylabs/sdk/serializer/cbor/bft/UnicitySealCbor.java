package org.unicitylabs.sdk.serializer.cbor.bft;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.unicitylabs.sdk.bft.InputRecord;
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

      ((CBORGenerator) gen).writeTag(1001);
      gen.writeStartArray(value, 8);
      gen.writeObject(value.getVersion());
      gen.writeObject(value.getNetworkId());
      gen.writeObject(value.getRootChainRoundNumber());
      gen.writeObject(value.getEpoch());
      gen.writeObject(value.getTimestamp());
      gen.writeObject(value.getPreviousHash());
      gen.writeObject(value.getHash());
      if (value.getSignatures() == null) {
        gen.writeNull();
      } else {
        gen.writeStartObject(value.getSignatures(), value.getSignatures().size());
        for (Map.Entry<String, byte[]> entry : value.getSignatures().entrySet()) {
          gen.writeFieldName(entry.getKey());
          gen.writeObject(entry.getValue());
        }
        gen.writeEndObject();
      }

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

      int version = p.readValueAs(int.class);
      short networkId = p.readValueAs(short.class);
      long rootChainRoundNumber = p.readValueAs(long.class);
      long epoch = p.readValueAs(long.class);
      long timestamp = p.readValueAs(long.class);
      byte[] previousHash = p.readValueAs(byte[].class);
      byte[] hash = p.readValueAs(byte[].class);

      if (p.nextToken() != JsonToken.START_OBJECT) {
        throw MismatchedInputException.from(p, UnicitySeal.class, "Expected map value");
      }

      Map<String, byte[]> signatures = new LinkedHashMap<>();
      while (p.nextToken() != JsonToken.END_OBJECT) {
        String name = p.currentName();
        p.nextToken();
        signatures.put(name, p.readValueAs(byte[].class));
      }

      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(p, InputRecord.class, "Expected end of array");
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
