package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;

public class SubmitCommitmentRequest {

  private final RequestId requestId;
  private final DataHash transactionHash;
  private final Authenticator authenticator;
  private final Boolean receipt;

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

  @JsonGetter("requestId")
  public RequestId getRequestId() {
    return this.requestId;
  }

  @JsonGetter("transactionHash")
  public DataHash getTransactionHash() {
    return this.transactionHash;
  }

  @JsonGetter("authenticator")
  public Authenticator getAuthenticator() {
    return this.authenticator;
  }

  @JsonGetter("receipt")
  public Boolean getReceipt() {
    return this.receipt;
  }

  public static SubmitCommitmentRequest fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, SubmitCommitmentRequest.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(SubmitCommitmentRequest.class, e);
    }
  }

  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(SubmitCommitmentRequest.class, e);
    }
  }
}
