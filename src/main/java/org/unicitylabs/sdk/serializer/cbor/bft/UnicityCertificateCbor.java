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
import org.unicitylabs.sdk.bft.InputRecord;
import org.unicitylabs.sdk.bft.ShardTreeCertificate;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.bft.UnicitySeal;
import org.unicitylabs.sdk.bft.UnicityTreeCertificate;

public class UnicityCertificateCbor {

  private UnicityCertificateCbor() {
  }

  public static class Serializer extends JsonSerializer<UnicityCertificate> {

    @Override
    public void serialize(UnicityCertificate value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      ((CBORGenerator) gen).writeTag(1007);
      gen.writeStartArray(value, 7);
      gen.writeObject(value.getVersion());
      gen.writeObject(value.getInputRecord());
      gen.writeObject(value.getTechnicalRecordHash());
      gen.writeObject(value.getShardConfigurationHash());
      gen.writeObject(value.getShardTreeCertificate());
      gen.writeObject(value.getUnicityTreeCertificate());
      gen.writeObject(value.getUnicitySeal());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<UnicityCertificate> {

    @Override
    public UnicityCertificate deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, UnicityCertificate.class, "Expected array value");
      }
      p.nextToken();

      UnicityCertificate result = new UnicityCertificate(
          p.readValueAs(int.class),
          p.readValueAs(InputRecord.class),
          p.readValueAs(byte[].class),
          p.readValueAs(byte[].class),
          p.readValueAs(ShardTreeCertificate.class),
          p.readValueAs(UnicityTreeCertificate.class),
          p.readValueAs(UnicitySeal.class)
      );

      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(p, UnicityCertificate.class, "Expected end of array");
      }

      return result;
    }
  }
}
