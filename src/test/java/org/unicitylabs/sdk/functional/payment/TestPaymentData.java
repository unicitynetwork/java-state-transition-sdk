package org.unicitylabs.sdk.functional.payment;

import org.unicitylabs.sdk.payment.PaymentData;
import org.unicitylabs.sdk.payment.asset.PaymentAssetCollection;

public class TestPaymentData implements PaymentData {

  private final PaymentAssetCollection assets;

  public TestPaymentData(PaymentAssetCollection assets) {
    this.assets = assets;
  }

  @Override
  public PaymentAssetCollection getAssets() {
    return this.assets;
  }

  public static TestPaymentData decode(byte[] bytes) {
    return new TestPaymentData(PaymentAssetCollection.fromCbor(bytes));
  }

  @Override
  public byte[] encode() {
    return this.assets.toCbor();
  }
}
