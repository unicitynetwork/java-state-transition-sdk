package org.unicitylabs.sdk.hash;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataHashTest {

  @Test
  public void testInvalidDataHashArguments() {
    NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
        () -> new DataHash(null, new byte[32]));
    Assertions.assertEquals("algorithm cannot be null", exception.getMessage());
    exception = Assertions.assertThrows(NullPointerException.class,
        () -> new DataHash(HashAlgorithm.SHA256, null));
    Assertions.assertEquals("data cannot be null", exception.getMessage());
  }

  @Test
  public void testDataHashJsonSerialization() throws JsonProcessingException {
    ObjectMapper objectMapper = UnicityObjectMapper.JSON;
    Assertions.assertEquals("null", objectMapper.writeValueAsString(null));
    Assertions.assertEquals(
        "\"00000000000000000000000000000000000000000000000000000000000000000000\"",
        objectMapper.writeValueAsString(new DataHash(HashAlgorithm.SHA256, new byte[32])));
    Assertions.assertEquals("\"000200000000000000000000000000000000\"",
        objectMapper.writeValueAsString(new DataHash(HashAlgorithm.SHA384, new byte[16])));

    Assertions.assertEquals(new DataHash(HashAlgorithm.SHA256, new byte[32]),
        objectMapper.readValue(
            "\"00000000000000000000000000000000000000000000000000000000000000000000\"",
            DataHash.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("[]", DataHash.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("\"AABBGG\"", DataHash.class));
  }
}
