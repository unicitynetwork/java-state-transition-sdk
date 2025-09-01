package com.unicity.sdk.transaction;

import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.util.HexConverter;

public class MintTransactionState extends RequestId {
  private static final byte[] MINT_SUFFIX = HexConverter.decode(
      "9e82002c144d7c5796c50f6db50a0c7bbd7f717ae3af6c6c71a3e9eba3022730");

  private MintTransactionState(DataHash hash) {
    super(hash);
  }

  public static MintTransactionState create(TokenId tokenId) {
    return new MintTransactionState(RequestId.createFromImprint(tokenId.getBytes(), MINT_SUFFIX).getHash());
  }
}
