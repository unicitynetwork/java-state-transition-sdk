
package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Objects;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.api.BlockHeightResponse;
import org.unicitylabs.sdk.api.CertificationData;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;

/**
 * Commitment representing a transfer transaction.
 */
public class TransferCommitment extends Commitment<TransferTransaction.Data> {

  @JsonCreator
  private TransferCommitment(
      @JsonProperty("transactionData")
      TransferTransaction.Data transactionData,
      @JsonProperty("certificationData")
      CertificationData certificationData
  ) {
    super(transactionData, certificationData);
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
   * Create transfer commitment from JSON string.
   *
   * @param input JSON string
   * @return transfer commitment data
   */
  public TransferCommitment fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, TransferCommitment.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(BlockHeightResponse.class, e);
    }
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

    TransferTransaction.Data transactionData = new TransferTransaction.Data(
        token.getState(),
        recipient,
        salt,
        recipientDataHash,
        message,
        token.getNametags()
    );

    CertificationData certificationData = CertificationData.create(
        transactionData.getSourceState().calculateHash(),
        transactionData.calculateHash(),
        signingService
    );

    return new TransferCommitment(transactionData, certificationData);
  }
}
