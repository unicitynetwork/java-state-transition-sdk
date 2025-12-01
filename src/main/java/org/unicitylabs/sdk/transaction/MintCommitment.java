
package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Objects;
import org.unicitylabs.sdk.api.BlockHeightResponse;
import org.unicitylabs.sdk.api.CertificationData;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
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
      @JsonProperty("transactionData")
      MintTransaction.Data<R> transactionData,
      @JsonProperty("authenticator")
      CertificationData certificationData
  ) {
    super(transactionData, certificationData);
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
   * Create mint commitment from JSON string.
   *
   * @param input JSON string
   * @return mint commitment data
   */
  public MintCommitment<?> fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, MintCommitment.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(BlockHeightResponse.class, e);
    }
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

    SigningService signingService = MintSigningService.create(transactionData.getTokenId());
    CertificationData certificationData = CertificationData.create(
        transactionData.getSourceState(),
        transactionData.calculateHash(),
        signingService
    );

    return new MintCommitment<>(transactionData, certificationData);
  }
}
