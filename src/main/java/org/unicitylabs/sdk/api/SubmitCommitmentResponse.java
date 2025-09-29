package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;

public class SubmitCommitmentResponse {

  private final SubmitCommitmentStatus status;

  @JsonCreator
  public SubmitCommitmentResponse(
      @JsonProperty("status") SubmitCommitmentStatus status
  ) {
    this.status = status;
  }

  @JsonGetter("status")
  public SubmitCommitmentStatus getStatus() {
    return this.status;
  }

  public static SubmitCommitmentResponse fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, SubmitCommitmentResponse.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(SubmitCommitmentResponse.class, e);
    }
  }

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
