package com.unicity.sdk.transaction;

import com.unicity.sdk.address.Address;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.token.NameTagTokenState;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class NameTagMintTransactionData extends MintTransactionData<MintTransactionReason> {
  private NameTagMintTransactionData(
      TokenId tokenId,
      TokenType tokenType,
      byte[] tokenData,
      TokenCoinData coinData,
      Address recipient,
      byte[] salt,
      DataHash dataHash
  ) {
    super(tokenId, tokenType, tokenData, coinData, recipient, salt, dataHash, null);
  }

  public static NameTagMintTransactionData create(
      String name,
      TokenType tokenType,
      byte[] tokenData,
      TokenCoinData coinData,
      Address recipient,
      byte[] salt,
      NameTagTokenState state
  ) throws IOException {
    Objects.requireNonNull(name, "Name cannot be null");
    Objects.requireNonNull(state, "State cannot be null");

    return new NameTagMintTransactionData(
        new TokenId(
            new DataHasher(HashAlgorithm.SHA256)
                .update(name.getBytes(StandardCharsets.UTF_8))
                .digest()
                .getImprint()
        ),
        tokenType,
        tokenData,
        coinData,
        recipient,
        salt,
        new DataHasher(HashAlgorithm.SHA256)
            .update(state.getData())
            .digest()
    );
  }
}