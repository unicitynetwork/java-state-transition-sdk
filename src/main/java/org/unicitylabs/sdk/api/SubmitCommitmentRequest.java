package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;

/**
 * Submit commitment request.
 */
public class SubmitCommitmentRequest {

  private final RequestId requestId;
  private final DataHash transactionHash;
  private final Authenticator authenticator;
  private final Boolean receipt;

  /**
   * Create submit commitment request.
   *
   * @param requestId       request id
   * @param transactionHash transaction hash
   * @param authenticator   authenticator
   * @param receipt         get receipt
   */
  @JsonCreator
  public SubmitCommitmentRequest(
      @JsonProperty("requestId") RequestId requestId,
      @JsonProperty("transactionHash") DataHash transactionHash,
      @JsonProperty("authenticator") Authenticator authenticator,
      @JsonProperty("receipt") Boolean receipt) {
    this.requestId = requestId;
    this.transactionHash = transactionHash;
    this.authenticator = authenticator;
    this.receipt = receipt;
  }

  /**
   * Get request id.
   *
   * @return request id
   */
  public RequestId getRequestId() {
    return this.requestId;
  }

  /**
   * Get transaction hash.
   *
   * @return transaction hash
   */
  public DataHash getTransactionHash() {
    return this.transactionHash;
  }

  /**
   * Get authenticator.
   *
   * @return authenticator
   */
  public Authenticator getAuthenticator() {
    return this.authenticator;
  }

  /**
   * Is getting receipt from unicity service.
   *
   * @return true if receipt unicity service should return receipt
   */
  public Boolean getReceipt() {
    return this.receipt;
  }

  /**
   * Create submit commitment request from JSON string.
   *
   * @param input JSON string
   * @return submit commitment request
   */
  public static SubmitCommitmentRequest fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, SubmitCommitmentRequest.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(SubmitCommitmentRequest.class, e);
    }
  }

  /**
   * Convert submit commitment request to JSON string.
   *
   * @return JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(SubmitCommitmentRequest.class, e);
    }
  }
}
