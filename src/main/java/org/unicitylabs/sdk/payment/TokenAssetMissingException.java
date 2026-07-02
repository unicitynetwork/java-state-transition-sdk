package org.unicitylabs.sdk.payment;

import org.unicitylabs.sdk.payment.asset.AssetId;

/**
 * Thrown when a split request references an asset the source token does not contain.
 */
public class TokenAssetMissingException extends IllegalArgumentException {

  /**
   * Create the exception.
   *
   * @param assetId missing asset id
   */
  public TokenAssetMissingException(AssetId assetId) {
    super(String.format("Token did not contain asset %s.", assetId));
  }
}
