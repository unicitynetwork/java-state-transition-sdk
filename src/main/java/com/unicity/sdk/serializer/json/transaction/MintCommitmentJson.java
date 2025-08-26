package com.unicity.sdk.serializer.json.transaction;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.transaction.MintCommitment;
import com.unicity.sdk.transaction.MintTransactionData;
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
