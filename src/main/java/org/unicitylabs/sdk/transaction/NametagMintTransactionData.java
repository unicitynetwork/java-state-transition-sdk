package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import java.nio.charset.StandardCharsets;

public class NametagMintTransactionData<R extends MintTransactionReason> extends
    MintTransactionData<R> {

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