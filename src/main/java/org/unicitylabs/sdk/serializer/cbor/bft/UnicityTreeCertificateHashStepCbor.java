package org.unicitylabs.sdk.serializer.cbor.bft;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import java.math.BigInteger;
import org.unicitylabs.sdk.bft.UnicityTreeCertificate;

public class UnicityTreeCertificateHashStepCbor {

  private UnicityTreeCertificateHashStepCbor() {
  }

  public static class Serializer extends JsonSerializer<UnicityTreeCertificate.HashStep> {

    @Override
    public void serialize(UnicityTreeCertificate.HashStep value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 0);
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<UnicityTreeCertificate.HashStep> {

    @Override
    public UnicityTreeCertificate.HashStep deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, UnicityTreeCertificate.class, "Expected array value");
      }
      p.nextToken();

      return new UnicityTreeCertificate.HashStep(
          p.readValueAs(BigInteger.class),
          p.readValueAs(byte[].class)
      );
    }
  }
}
