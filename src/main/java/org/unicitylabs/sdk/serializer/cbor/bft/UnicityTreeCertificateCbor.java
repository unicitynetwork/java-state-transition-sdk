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
import java.util.ArrayList;
import java.util.List;
import org.unicitylabs.sdk.bft.UnicityTreeCertificate;

public class UnicityTreeCertificateCbor {

  private UnicityTreeCertificateCbor() {
  }

  public static class Serializer extends JsonSerializer<UnicityTreeCertificate> {

    @Override
    public void serialize(UnicityTreeCertificate value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      ((CBORGenerator) gen).writeTag(1014);
      gen.writeStartArray(value, 3);
      gen.writeObject(value.getVersion());
      gen.writeObject(value.getPartitionIdentifier());
      gen.writeObject(value.getSteps());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<UnicityTreeCertificate> {

    @Override
    public UnicityTreeCertificate deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, UnicityTreeCertificate.class, "Expected array value");
      }
      p.nextToken();

      int version = p.readValueAs(int.class);
      int partitionIdentifier = p.readValueAs(int.class);

      if (p.nextToken() != JsonToken.START_ARRAY) {
        throw MismatchedInputException.from(p, UnicityTreeCertificate.class, "Expected hash step list");
      }

      List<UnicityTreeCertificate.HashStep> steps = new ArrayList<>();
      while (p.nextToken() != JsonToken.END_ARRAY) {
        steps.add(ctx.readValue(p, UnicityTreeCertificate.HashStep.class));
      }

      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(p, UnicityTreeCertificate.HashStep.class, "Expected end of array");
      }

      return new UnicityTreeCertificate(version, partitionIdentifier, steps);
    }
  }
}
