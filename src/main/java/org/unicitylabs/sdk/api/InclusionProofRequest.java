package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;

/**
 * Inclusion proof request.
 */
public class InclusionProofRequest {

  private final RequestId requestId;

  /**
   * Create inclusion proof request.
   *
   * @param requestId request id
   */
  @JsonCreator
  public InclusionProofRequest(
      @JsonProperty("requestId") RequestId requestId
  ) {
    this.requestId = requestId;
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
   * Create request from JSON string.
   *
   * @param input JSON string
   * @return inclusion proof request
   */
  public static InclusionProofRequest fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, InclusionProofRequest.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(InclusionProofRequest.class, e);
    }
  }

  /**
   * Convert request to JSON string.
   *
   * @return JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(InclusionProofRequest.class, e);
    }
  }
}
