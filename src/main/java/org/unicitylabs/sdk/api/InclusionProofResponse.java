package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.transaction.InclusionProof;

/**
 * Inclusion proof response.
 */
public class InclusionProofResponse {

  private final InclusionProof inclusionProof;

  /**
   * Create inclison proof response.
   *
   * @param inclusionProof inclusion proof
   */
  @JsonCreator
  public InclusionProofResponse(
      @JsonProperty("inclusionProof")
      InclusionProof inclusionProof
  ) {
    this.inclusionProof = inclusionProof;
  }

  /**
   * Get inclusion proof.
   *
   * @return inclusion proof
   */
  public InclusionProof getInclusionProof() {
    return this.inclusionProof;
  }

  /**
   * Create response from JSON string.
   *
   * @param input JSON string
   * @return inclusion proof response
   */
  public static InclusionProofResponse fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, InclusionProofResponse.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(InclusionProofResponse.class, e);
    }
  }

  /**
   * Convert response to JSON string.
   *
   * @return JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(InclusionProofResponse.class, e);
    }
  }
}
