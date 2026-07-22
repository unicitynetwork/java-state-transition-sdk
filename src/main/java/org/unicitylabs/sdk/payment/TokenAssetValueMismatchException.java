package org.unicitylabs.sdk.payment;

import org.unicitylabs.sdk.payment.asset.AssetId;

import java.math.BigInteger;

/**
 * Thrown when the split outputs for an asset do not sum to the source token's asset value.
 */
public class TokenAssetValueMismatchException extends IllegalArgumentException {

  /**
   * Create the exception.
   *
   * @param assetId asset id
   * @param value source token asset value
   * @param splitValue total value committed by the split outputs
   */
  public TokenAssetValueMismatchException(AssetId assetId, BigInteger value,
                                          BigInteger splitValue) {
    super(String.format("Token contained %s %s assets, but tree has %s", value, assetId,
            splitValue));
  }
}
