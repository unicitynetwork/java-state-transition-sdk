package org.unicitylabs.sdk.payment;

import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.transaction.TokenSalt;

import java.util.Objects;

/**
 * Request to mint one new token as part of a token split. Splitting preserves the source token
 * type, so the output token type is not chosen here. The payment data carries both the output's
 * assets and its self-encoding, so each output may embed its own token-type-specific payload
 * alongside the asset allocation.
 */
public class SplitTokenRequest {

  private final Predicate recipient;
  private final PaymentData paymentData;
  private final TokenSalt salt;

  private SplitTokenRequest(Predicate recipient, PaymentData paymentData, TokenSalt salt) {
    this.recipient = recipient;
    this.paymentData = paymentData;
    this.salt = salt;
  }

  /**
   * Create a SplitTokenRequest.
   *
   * @param recipient predicate that will lock the new token
   * @param paymentData payment data the new token will carry; its assets are allocated from the
   *     source and its {@code encode()} produces the exact minted payload
   * @param salt salt for the new token
   * @return new request
   */
  public static SplitTokenRequest create(Predicate recipient, PaymentData paymentData,
                                         TokenSalt salt) {
    Objects.requireNonNull(recipient, "Recipient cannot be null");
    Objects.requireNonNull(paymentData, "Payment data cannot be null");
    Objects.requireNonNull(salt, "Salt cannot be null");

    return new SplitTokenRequest(recipient, paymentData, salt);
  }

  /**
   * Create a SplitTokenRequest with a random salt.
   *
   * @param recipient predicate that will lock the new token
   * @param paymentData payment data the new token will carry
   * @return new request
   */
  public static SplitTokenRequest create(Predicate recipient, PaymentData paymentData) {
    return SplitTokenRequest.create(recipient, paymentData, TokenSalt.generate());
  }

  public Predicate getRecipient() {
    return this.recipient;
  }

  public PaymentData getPaymentData() {
    return this.paymentData;
  }

  public TokenSalt getSalt() {
    return this.salt;
  }
}
