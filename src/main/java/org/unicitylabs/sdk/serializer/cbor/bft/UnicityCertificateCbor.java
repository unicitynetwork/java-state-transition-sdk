package org.unicitylabs.sdk.serializer.cbor.bft;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import java.io.IOException;
import java.math.BigInteger;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.bft.InputRecord;
import org.unicitylabs.sdk.bft.ShardTreeCertificate;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.bft.UnicitySeal;
import org.unicitylabs.sdk.bft.UnicityTreeCertificate;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.signing.Signature;
import org.unicitylabs.sdk.util.HexConverter;

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

      gen.writeStartArray(value, 0);
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
          p.readValueAs(BigInteger.class),
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
