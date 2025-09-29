package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import java.math.BigInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RequestIdTest {

  @Test
  public void shouldResolveToBigInteger() {
    RequestId requestId = RequestId.create(new byte[5],
        new DataHash(HashAlgorithm.SHA256, new byte[32]));
    Assertions.assertEquals(
        new BigInteger(
            "7588617643772589565921291111125869131233840654380505021472016115258380142349673042"
        ),
        requestId.toBitString().toBigInteger());
  }

  @Test
  public void testJsonSerialization() throws JsonProcessingException {
    RequestId requestId = RequestId.create(
        new byte[5],
        new DataHash(HashAlgorithm.SHA256, new byte[32])
    );

    Assertions.assertEquals(
        requestId,
        RequestId.fromJson(requestId.toJson())
    );
  }
}
