package com.unicity.sdk.mtree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStep;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStepBranch;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.HashAlgorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MerkleTreePathStepTest {

  @Test
  public void testConstructorThrowsOnNullArguments() {
    Exception exception = assertThrows(NullPointerException.class, () -> {
      new SparseMerkleTreePathStep(null, null,  null);
    });
    assertEquals("path cannot be null", exception.getMessage());
  }

  @Test
  public void testJsonSerialization() throws JsonProcessingException {
    ObjectMapper objectMapper = UnicityObjectMapper.JSON;

    JsonMappingException exception = Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"path\":\"asd\",\"sibling\":null,\"branch\":null}",
            SparseMerkleTreePathStep.class));
    exception = Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"path\":[],\"sibling\":null,\"branch\":null}",
            SparseMerkleTreePathStep.class));
    exception = Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"sibling\":null,\"branch\":null}",
            SparseMerkleTreePathStep.class));
    exception = Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"path\":\"5\",\"branch\":null}", SparseMerkleTreePathStep.class));
    exception = Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"path\":\"5\",\"sibling\":null,\"branch\":\"asd\"}",
            SparseMerkleTreePathStep.class));

    SparseMerkleTreePathStep step = new SparseMerkleTreePathStep(
        BigInteger.ONE,
        new DataHash(HashAlgorithm.SHA384, new byte[5]),
        new SparseMerkleTreePathStepBranch(new byte[3])
    );
    Assertions.assertEquals(step,
        objectMapper.readValue(objectMapper.writeValueAsString(step), SparseMerkleTreePathStep.class));

  }
}
