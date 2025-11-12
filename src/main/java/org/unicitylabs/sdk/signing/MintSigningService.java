package org.unicitylabs.sdk.signing;

import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Signing service for minting operations.
 */
public class MintSigningService {
  private static final byte[] MINTER_SECRET = HexConverter.decode(
      "495f414d5f554e4956455253414c5f4d494e5445525f464f525f");

  private MintSigningService() {}

  /**
   * Create signing service for minting operations.
   *
   * @param tokenId token identifier
   * @return signing service
   */
  public static SigningService create(TokenId tokenId) {
    return SigningService.createFromMaskedSecret(MINTER_SECRET, tokenId.getBytes());
  }
}
