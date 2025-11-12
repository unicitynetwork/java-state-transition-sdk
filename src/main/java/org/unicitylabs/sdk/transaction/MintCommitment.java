
package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.signing.MintSigningService;
import org.unicitylabs.sdk.signing.SigningService;

/**
 * Commitment representing a submitted transaction.
 *
 * @param <R> the type of transaction data
 */
public class MintCommitment<R extends MintTransactionReason> extends
    Commitment<MintTransaction.Data<R>> {
  @JsonCreator
  private MintCommitment(
      @JsonProperty("requestId")
      RequestId requestId,
      @JsonProperty("transactionData")
      MintTransaction.Data<R> transactionData,
      @JsonProperty("authenticator")
      Authenticator authenticator
  ) {
    super(requestId, transactionData, authenticator);
  }

  /**
   * Create mint transaction from commitment.
   *
   * @param inclusionProof Commitment inclusion proof
   * @return mint transaction
   */
  @Override
  public MintTransaction<R> toTransaction(InclusionProof inclusionProof) {
    return new MintTransaction<>(this.getTransactionData(), inclusionProof);
  }

  /**
   * Create mint commitment from transaction data.
   *
   * @param data mint transaction data
   * @param <R>             mint reason
   * @return mint commitment
   */
  public static <R extends MintTransactionReason> MintCommitment<R> create(
      MintTransaction.Data<R> data
  ) {
    Objects.requireNonNull(data, "Transaction data cannot be null");

    SigningService signingService = MintSigningService.create(data.getTokenId());
    return new MintCommitment<>(
        RequestId.create(signingService.getPublicKey(), data.getSourceState()),
        data,
        Authenticator.create(
            signingService,
            data.calculateHash(),
            data.getSourceState()
        )
    );
  }
}
