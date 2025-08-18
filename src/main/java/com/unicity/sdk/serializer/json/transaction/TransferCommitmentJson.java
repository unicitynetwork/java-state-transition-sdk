package com.unicity.sdk.serializer.json.transaction;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.transaction.MintCommitment;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.TransferCommitment;
import com.unicity.sdk.transaction.TransferTransactionData;
import java.io.IOException;

public class TransferCommitmentJson {

  private TransferCommitmentJson() {}

  public static class Deserializer extends CommitmentJson.Deserializer<TransferTransactionData, TransferCommitment> {
    @Override
    protected TransferTransactionData createTransactionData(JsonParser p, DeserializationContext ctx) throws IOException {
      return p.readValueAs(TransferTransactionData.class);
    }

    @Override
    protected TransferCommitment createCommitment(
        RequestId requestId, TransferTransactionData transactionData, Authenticator authenticator) {
      return new TransferCommitment(requestId, transactionData, authenticator);
    }
  }
}
