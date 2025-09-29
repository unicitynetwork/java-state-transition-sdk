package org.unicitylabs.sdk.transaction;

import java.nio.charset.StandardCharsets;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;

public class NametagMintTransactionData<R extends MintTransactionReason> extends
    MintTransaction.Data<R> {

  public NametagMintTransactionData(
      String name,
      TokenType tokenType,
      Address recipient,
      byte[] salt,
      Address targetAddress
  ) {
    super(
        TokenId.fromNameTag(name),
        tokenType,
        targetAddress.getAddress().getBytes(StandardCharsets.UTF_8),
        null,
        recipient,
        salt,
        null,
        null
    );
  }
}