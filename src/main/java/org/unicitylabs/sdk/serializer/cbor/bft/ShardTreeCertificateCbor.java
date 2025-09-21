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
import java.util.ArrayList;
import java.util.List;
import org.unicitylabs.sdk.bft.ShardTreeCertificate;

public class ShardTreeCertificateCbor {

  private ShardTreeCertificateCbor() {
  }

  public static class Serializer extends JsonSerializer<ShardTreeCertificate> {

    @Override
    public void serialize(ShardTreeCertificate value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 0);
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<ShardTreeCertificate> {

    @Override
    public ShardTreeCertificate deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, ShardTreeCertificate.class, "Expected array value");
      }
      p.nextToken();
      byte[] shard = p.readValueAs(byte[].class);
      if (p.nextToken() != JsonToken.START_ARRAY) {
        throw MismatchedInputException.from(p, ShardTreeCertificate.class, "Expected sibling hash list");
      }

      List<byte[]> siblings = new ArrayList<>();
      while (p.nextToken() != JsonToken.END_ARRAY) {
        siblings.add(p.readValueAs(byte[].class));
      }

      ShardTreeCertificate result = new ShardTreeCertificate(shard, siblings);

      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(p, ShardTreeCertificate.class, "Expected end of array");
      }

      return result;
    }
  }
}
