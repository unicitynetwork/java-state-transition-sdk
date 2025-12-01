package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.api.StateId;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Token mint state.
 */
public class MintTransactionState extends DataHash {

  private static final byte[] MINT_SUFFIX = HexConverter.decode(
      "9e82002c144d7c5796c50f6db50a0c7bbd7f717ae3af6c6c71a3e9eba3022730");

  private MintTransactionState(DataHash hash) {
    super(hash.getAlgorithm(), hash.getData());
  }

  /**
   * Create token initial state from token id.
   *
   * @param tokenId token id
   * @return mint state
   */
  public static MintTransactionState create(TokenId tokenId) {
    return new MintTransactionState(StateId.create(tokenId.getBytes(), MINT_SUFFIX));
  }
}
