package org.unicitylabs.sdk.serializer.cbor.transaction;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.unicitylabs.sdk.transaction.MintTransactionReason;
import org.unicitylabs.sdk.transaction.split.SplitMintReason;
import java.io.IOException;

public class MintTransactionReasonCbor {

  private MintTransactionReasonCbor() {
  }

  public static class Deserializer extends JsonDeserializer<MintTransactionReason> {

    @Override
    public MintTransactionReason deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      return p.readValueAs(SplitMintReason.class);
    }
  }
}