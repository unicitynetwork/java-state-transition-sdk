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
import java.nio.charset.StandardCharsets;
import org.unicitylabs.sdk.bft.InputRecord;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.util.HexConverter;

public class InputRecordCbor {

  private InputRecordCbor() {
  }

  public static class Serializer extends JsonSerializer<InputRecord> {

    @Override
    public void serialize(InputRecord value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 0);
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<InputRecord> {

    @Override
    public InputRecord deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, InputRecord.class, "Expected array value");
      }
      p.nextToken();

      BigInteger version = p.readValueAs(BigInteger.class);

      BigInteger roundNumber = p.readValueAs(BigInteger.class);
      BigInteger epoch = p.readValueAs(BigInteger.class);
      byte[] previousHash = p.readValueAs(byte[].class);
      byte[] hash = p.readValueAs(byte[].class);
      byte[] summaryValue = p.readValueAs(byte[].class);
      BigInteger timestamp = p.readValueAs(BigInteger.class);
      byte[] blockHash = p.readValueAs(byte[].class);
      BigInteger sumOfEarnedFees = p.readValueAs(BigInteger.class);
      byte[] executedTransactionsHash = p.readValueAs(byte[].class);


      InputRecord result = new InputRecord(
          version,
          roundNumber,
          epoch,
          previousHash != null
              ? HexConverter.decode(new String(previousHash, StandardCharsets.UTF_8))
              : null,
          hash != null
              ? HexConverter.decode(new String(hash, StandardCharsets.UTF_8))
              : null,
          summaryValue,
          timestamp,
          blockHash,
          sumOfEarnedFees,
          executedTransactionsHash
      );

      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(p, InputRecord.class, "Expected end of array");
      }

      return result;
    }
  }
}
