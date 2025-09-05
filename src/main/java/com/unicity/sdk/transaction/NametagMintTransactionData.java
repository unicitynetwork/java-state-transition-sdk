package com.unicity.sdk.transaction;

import com.unicity.sdk.address.Address;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;
import java.nio.charset.StandardCharsets;

public class NametagMintTransactionData<R extends MintTransactionReason> extends MintTransactionData<R> {
  public NametagMintTransactionData(
      String name,
      TokenType tokenType,
      byte[] tokenData,
      TokenCoinData coinData,
      Address recipient,
      byte[] salt,
      Address targetAddress
  ) {
    super(
        TokenId.fromNameTag(name),
        tokenType,
        tokenData,
        coinData,
        recipient,
        salt,
        new DataHasher(HashAlgorithm.SHA256)
            .update(targetAddress.getAddress().getBytes(StandardCharsets.UTF_8))
            .digest(),
        null
    );
  }
}