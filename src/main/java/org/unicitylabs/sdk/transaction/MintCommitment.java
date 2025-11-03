
package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Commitment representing a submitted transaction.
 *
 * @param <R> the type of transaction data
 */
public class MintCommitment<R extends MintTransactionReason> extends
    Commitment<MintTransaction.Data<R>> {
  private static final byte[] MINTER_SECRET = HexConverter.decode(
      "495f414d5f554e4956455253414c5f4d494e5445525f464f525f");

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
   * @param transactionData mint transaction data
   * @param <R>             mint reason
   * @return mint commitment
   */
  public static <R extends MintTransactionReason> MintCommitment<R> create(
      MintTransaction.Data<R> transactionData
  ) {
    Objects.requireNonNull(transactionData, "Transaction data cannot be null");

    SigningService signingService = MintCommitment.createSigningService(transactionData);

    DataHash transactionHash = transactionData.calculateHash();

    RequestId requestId = RequestId.create(
        signingService.getPublicKey(),
        transactionData.getSourceState()
    );
    Authenticator authenticator = Authenticator.create(
        signingService,
        transactionHash,
        transactionData.getSourceState()
    );

    return new MintCommitment<>(requestId, transactionData, authenticator);
  }


  /**
   * Create signing service for initial mint.
   *
   * @param transactionData mint transaction data
   * @return signing service
   */
  public static SigningService createSigningService(MintTransaction.Data<?> transactionData) {
    return SigningService.createFromMaskedSecret(MINTER_SECRET,
        transactionData.getTokenId().getBytes());
  }
}
