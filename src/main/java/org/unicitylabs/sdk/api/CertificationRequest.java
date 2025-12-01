package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;

/**
 * Submit certification request.
 */
public class CertificationRequest {

  private final StateId stateId;
  private final CertificationData certificationData;
  private final Boolean receipt;

  /**
   * Create certification request.
   *
   * @param stateId           state id
   * @param certificationData transaction hash
   * @param receipt           get receipt
   */
  @JsonCreator
  private CertificationRequest(
      @JsonProperty("stateId") StateId stateId,
      @JsonProperty("certificationData") CertificationData certificationData,
      @JsonProperty("receipt") Boolean receipt) {
    this.stateId = stateId;
    this.certificationData = certificationData;
    this.receipt = receipt;
  }

  /**
   * Get state id.
   *
   * @return state id
   */
  public StateId getStateId() {
    return this.stateId;
  }

  /**
   * Get certification data.
   *
   * @return certification data
   */
  public CertificationData getCertificationData() {
    return this.certificationData;
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
   * Create certification request.
   *
   * @param certificationData certification data
   * @param receipt           get receipt
   * @return certification request
   */
  public static CertificationRequest create(CertificationData certificationData, boolean receipt) {
    return new CertificationRequest(certificationData.calculateStateId(), certificationData, receipt);
  }

  /**
   * Convert certification request to JSON string.
   *
   * @return JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(CertificationRequest.class, e);
    }
  }
}
