package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.transaction.InclusionProof;

public class InclusionProofResponse {

  private final InclusionProof inclusionProof;

  @JsonCreator
  public InclusionProofResponse(
      @JsonProperty("inclusionProof")
      InclusionProof inclusionProof
  ) {
    this.inclusionProof = inclusionProof;
  }

  @JsonGetter("inclusionProof")
  public InclusionProof getInclusionProof() {
    return this.inclusionProof;
  }

  public static InclusionProofRequest fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, InclusionProofRequest.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(InclusionProofRequest.class, e);
    }
  }

  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(InclusionProofRequest.class, e);
    }
  }
}
