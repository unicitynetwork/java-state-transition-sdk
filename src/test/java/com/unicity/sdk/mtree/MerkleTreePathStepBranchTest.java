package com.unicity.sdk.mtree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStep.Branch;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MerkleTreePathStepBranchTest {

  @Test
  public void testJsonSerialization() throws JsonProcessingException {
    ObjectMapper objectMapper = UnicityObjectMapper.JSON;
    Assertions.assertEquals("null", objectMapper.writeValueAsString(null));
    Assertions.assertEquals("[null]",
        objectMapper.writeValueAsString(new Branch(null)));
    Assertions.assertEquals("[\"00000000\"]",
        objectMapper.writeValueAsString(new Branch(new byte[4])));
    Assertions.assertEquals(new Branch(null),
        objectMapper.readValue("[null]", Branch.class));
    Assertions.assertEquals(new Branch(new byte[]{(byte) 0x51, (byte) 0x55}),
        objectMapper.readValue("[\"5155\"]", Branch.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("\"asd\"", Branch.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("[\"AABBGG\"]", Branch.class));
  }
}
