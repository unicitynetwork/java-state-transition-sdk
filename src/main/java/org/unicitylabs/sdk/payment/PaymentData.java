package org.unicitylabs.sdk.payment;

import org.unicitylabs.sdk.payment.asset.PaymentAssetCollection;

/**
 * Represents payment payload data.
 */
public interface PaymentData {
  /**
   * Returns the assets included in this payment payload.
   *
   * @return assets in canonical asset-id order
   */
  PaymentAssetCollection getAssets();

  /**
   * Encodes this payment payload into bytes.
   *
   * @return encoded payment data
   */
  byte[] encode();
}
