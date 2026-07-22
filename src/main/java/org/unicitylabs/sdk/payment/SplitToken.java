package org.unicitylabs.sdk.payment;

import org.unicitylabs.sdk.api.NetworkId;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.transaction.TokenSalt;
import org.unicitylabs.sdk.transaction.TokenType;

import java.util.List;

/**
 * Realized split output: everything needed to mint the new token. Mint it with exactly
 * {@code getPaymentData().encode()} as the auxiliary payload - those are the bytes bound by the
 * split allocation proofs.
 */
public class SplitToken {

  private final NetworkId networkId;
  private final Predicate recipient;
  private final TokenType tokenType;
  private final TokenSalt salt;
  private final PaymentData paymentData;
  private final List<SplitAllocationProof> proofs;

  SplitToken(
          NetworkId networkId,
          Predicate recipient,
          TokenType tokenType,
          TokenSalt salt,
          PaymentData paymentData,
          List<SplitAllocationProof> proofs
  ) {
    this.networkId = networkId;
    this.recipient = recipient;
    this.tokenType = tokenType;
    this.salt = salt;
    this.paymentData = paymentData;
    this.proofs = List.copyOf(proofs);
  }

  public NetworkId getNetworkId() {
    return this.networkId;
  }

  public Predicate getRecipient() {
    return this.recipient;
  }

  public TokenType getTokenType() {
    return this.tokenType;
  }

  public TokenSalt getSalt() {
    return this.salt;
  }

  public PaymentData getPaymentData() {
    return this.paymentData;
  }

  public List<SplitAllocationProof> getProofs() {
    return this.proofs;
  }
}
