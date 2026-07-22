package org.unicitylabs.sdk.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CertificationResponseTest {

  @Test
  void ignoresUnknownFields() {
    CertificationResponse response =
        CertificationResponse.fromJson(
            "{\"status\":\"SUCCESS\",\"unknownField\":\"value\",\"another\":123}");

    Assertions.assertEquals(CertificationStatus.SUCCESS, response.getStatus());
  }
}
