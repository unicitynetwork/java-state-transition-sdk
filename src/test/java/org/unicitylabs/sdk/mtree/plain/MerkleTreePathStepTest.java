package org.unicitylabs.sdk.mtree.plain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import java.math.BigInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;

public class MerkleTreePathStepTest {

  @Test
  public void testConstructorThrowsOnNullArguments() {
    Exception exception = assertThrows(NullPointerException.class,
        () -> new SparseMerkleTreePathStep(null, null));
    assertEquals("path cannot be null", exception.getMessage());
  }

  @Test
  public void testJsonSerialization() throws JsonProcessingException {
    ObjectMapper objectMapper = UnicityObjectMapper.JSON;

    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"path\":\"asd\",\"sibling\":null,\"branch\":null}",
            SparseMerkleTreePathStep.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"path\":[],\"sibling\":null,\"branch\":null}",
            SparseMerkleTreePathStep.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"sibling\":null,\"branch\":null}",
            SparseMerkleTreePathStep.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"path\":\"5\",\"sibling\":null,\"branch\":\"asd\"}",
            SparseMerkleTreePathStep.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"path\":5,\"sibling\":null,\"branch\":\"null\"}",
            SparseMerkleTreePathStep.class));

    SparseMerkleTreePathStep step = new SparseMerkleTreePathStep(
        BigInteger.ONE,
        new DataHash(HashAlgorithm.SHA384, new byte[5]).getImprint()
    );
    Assertions.assertEquals(step,
        objectMapper.readValue(objectMapper.writeValueAsString(step),
            SparseMerkleTreePathStep.class));

  }
}
