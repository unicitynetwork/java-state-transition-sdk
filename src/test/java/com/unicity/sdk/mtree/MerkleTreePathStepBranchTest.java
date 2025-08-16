package com.unicity.sdk.mtree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStepBranch;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MerkleTreePathStepBranchTest {

  @Test
  public void testJsonSerialization() throws JsonProcessingException {
    ObjectMapper objectMapper = UnicityObjectMapper.JSON;
    Assertions.assertEquals("null", objectMapper.writeValueAsString(null));
    Assertions.assertEquals("[]",
        objectMapper.writeValueAsString(new SparseMerkleTreePathStepBranch(null)));
    Assertions.assertEquals("[\"00000000\"]",
        objectMapper.writeValueAsString(new SparseMerkleTreePathStepBranch(new byte[4])));
    Assertions.assertEquals(new SparseMerkleTreePathStepBranch(null),
        objectMapper.readValue("[]", SparseMerkleTreePathStepBranch.class));
    Assertions.assertEquals(new SparseMerkleTreePathStepBranch(null),
        objectMapper.readValue("[null]", SparseMerkleTreePathStepBranch.class));
    Assertions.assertEquals(new SparseMerkleTreePathStepBranch(new byte[]{(byte) 0x51, (byte) 0x55}),
        objectMapper.readValue("[\"5155\"]", SparseMerkleTreePathStepBranch.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("\"asd\"", SparseMerkleTreePathStepBranch.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("[\"AABBGG\"]", SparseMerkleTreePathStepBranch.class));
  }
}
