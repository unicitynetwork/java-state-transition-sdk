package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;

/**
 * Submit commitment response.
 */
public class SubmitCommitmentResponse {

  private final SubmitCommitmentStatus status;

  /**
   * Create submit commitment response.
   *
   * @param status status
   */
  @JsonCreator
  public SubmitCommitmentResponse(
      @JsonProperty("status") SubmitCommitmentStatus status
  ) {
    this.status = status;
  }

  /**
   * Get status.
   *
   * @return status
   */
  public SubmitCommitmentStatus getStatus() {
    return this.status;
  }

  /**
   * Create submit commitment response from JSON string.
   *
   * @param input JSON string
   * @return submit commitment response
   */
  public static SubmitCommitmentResponse fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, SubmitCommitmentResponse.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(SubmitCommitmentResponse.class, e);
    }
  }

  /**
   * Convert submit commitment response to JSON string.
   *
   * @return JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(SubmitCommitmentResponse.class, e);
    }
  }

  @Override
  public String toString() {
    return String.format("SubmitCommitmentResponse{status=%s}", this.status);
  }
}
