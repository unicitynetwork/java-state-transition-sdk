package org.unicitylabs.sdk.serializer.json.transaction;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.transaction.MintCommitment;
import org.unicitylabs.sdk.transaction.MintTransactionData;
import java.io.IOException;

public class MintCommitmentJson {

  private MintCommitmentJson() {}

  public static class Deserializer extends CommitmentJson.Deserializer<MintTransactionData<?>, MintCommitment<MintTransactionData<?>>> {
    @Override
    protected MintTransactionData<?> createTransactionData(JsonParser p, DeserializationContext ctx) throws IOException {
      return p.readValueAs(MintTransactionData.class);
    }

    @Override
    protected MintCommitment<MintTransactionData<?>> createCommitment(
        RequestId requestId, MintTransactionData<?> transactionData, Authenticator authenticator) {
      return new MintCommitment<>(requestId, transactionData, authenticator);
    }
  }
}
