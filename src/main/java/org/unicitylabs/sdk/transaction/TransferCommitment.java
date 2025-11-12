
package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;

/**
 * Commitment representing a transfer transaction.
 */
public class TransferCommitment extends Commitment<TransferTransaction.Data> {

  @JsonCreator
  private TransferCommitment(
      @JsonProperty("requestId")
      RequestId requestId,
      @JsonProperty("transactionData")
      TransferTransaction.Data transactionData,
      @JsonProperty("authenticator")
      Authenticator authenticator
  ) {
    super(requestId, transactionData, authenticator);
  }

  /**
   * Create transfer transaction from transfer commitment.
   *
   * @param inclusionProof Commitment inclusion proof
   * @return transfer transaction
   */
  @Override
  public TransferTransaction toTransaction(InclusionProof inclusionProof) {
    return new TransferTransaction(this.getTransactionData(), inclusionProof);
  }

  /**
   * Create transfer commitment.
   *
   * @param token             current token
   * @param recipient         recipient of token
   * @param salt              transaction salt
   * @param recipientDataHash recipient data hash
   * @param message           transaction message
   * @param signingService    signing service to unlock token
   * @return transfer commitment
   */
  public static TransferCommitment create(
      Token<?> token,
      Address recipient,
      byte[] salt,
      DataHash recipientDataHash,
      byte[] message,
      SigningService signingService
  ) {
    Objects.requireNonNull(token, "Token cannot be null");
    Objects.requireNonNull(recipient, "Recipient address cannot be null");
    Objects.requireNonNull(salt, "Salt cannot be null");
    Objects.requireNonNull(signingService, "SigningService cannot be null");

    TransferTransaction.Data data = new TransferTransaction.Data(
        token.getState(),
        recipient,
        salt,
        recipientDataHash,
        message,
        token.getNametags()
    );
    RequestId requestId = RequestId.create(signingService.getPublicKey(), data.getSourceState());
    Authenticator authenticator = Authenticator.create(
        signingService,
        data.calculateHash(),
        data.getSourceState().calculateHash()
    );

    return new TransferCommitment(requestId, data, authenticator);
  }
}
