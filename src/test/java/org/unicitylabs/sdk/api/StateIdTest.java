package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import java.math.BigInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StateIdTest {

  @Test
  public void shouldResolveToBigInteger() {
    StateId stateId = StateId.create(new byte[5],
        new DataHash(HashAlgorithm.SHA256, new byte[32]));
    Assertions.assertEquals(
        new BigInteger(
            "7588640947079736950651543599112501467726489700903461286985351382586976462842180672"
        ),
        stateId.toBitString().toBigInteger());
  }

  @Test
  public void testJsonSerialization() {
    StateId stateId = StateId.create(
        new byte[5],
        new DataHash(HashAlgorithm.SHA256, new byte[32])
    );

    Assertions.assertEquals(
        stateId,
        StateId.fromJson(stateId.toJson())
    );
  }
}
