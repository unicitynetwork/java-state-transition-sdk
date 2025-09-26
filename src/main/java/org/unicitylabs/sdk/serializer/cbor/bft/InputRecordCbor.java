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

      ((CBORGenerator) gen).writeTag(1008);
      gen.writeStartArray(value, 10);
      gen.writeObject(value.getVersion());
      gen.writeObject(value.getRoundNumber());
      gen.writeObject(value.getEpoch());
      gen.writeObject(value.getPreviousHash());
      gen.writeObject(value.getHash());
      gen.writeObject(value.getSummaryValue());
      gen.writeObject(value.getTimestamp());
      gen.writeObject(value.getBlockHash());
      gen.writeObject(value.getSumOfEarnedFees());
      gen.writeObject(value.getExecutedTransactionsHash());
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

      int version = p.readValueAs(int.class);
      long roundNumber = p.readValueAs(long.class);
      long epoch = p.readValueAs(long.class);
      byte[] previousHash = p.readValueAs(byte[].class);
      byte[] hash = p.readValueAs(byte[].class);
      byte[] summaryValue = p.readValueAs(byte[].class);
      long timestamp = p.readValueAs(long.class);
      byte[] blockHash = p.readValueAs(byte[].class);
      long sumOfEarnedFees = p.readValueAs(long.class);
      byte[] executedTransactionsHash = p.readValueAs(byte[].class);

      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(p, InputRecord.class, "Expected end of array");
      }

      return new InputRecord(
          version,
          roundNumber,
          epoch,
          previousHash,
          hash,
          summaryValue,
          timestamp,
          blockHash,
          sumOfEarnedFees,
          executedTransactionsHash
      );
    }
  }
}
