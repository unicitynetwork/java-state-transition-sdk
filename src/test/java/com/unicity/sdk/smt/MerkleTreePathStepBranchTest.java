package com.unicity.sdk.smt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.smt.path.MerkleTreePathStepBranch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MerkleTreePathStepBranchTest {
    @Test
    public void testJsonSerialization() throws JsonProcessingException {
        ObjectMapper objectMapper = UnicityObjectMapper.JSON;
        Assertions.assertEquals("null", objectMapper.writeValueAsString(null));
        Assertions.assertEquals("[]", objectMapper.writeValueAsString(new MerkleTreePathStepBranch(null)));
        Assertions.assertEquals("[\"00000000\"]", objectMapper.writeValueAsString(new MerkleTreePathStepBranch(new byte[4])));
        Assertions.assertEquals(new MerkleTreePathStepBranch(null), objectMapper.readValue("[null]", MerkleTreePathStepBranch.class));
        Assertions.assertEquals(new MerkleTreePathStepBranch(new byte[] { (byte) 0x51, (byte) 0x55 }), objectMapper.readValue("[\"5155\"]", MerkleTreePathStepBranch.class));
        Assertions.assertThrows(JsonMappingException.class, () -> objectMapper.readValue("\"asd\"", MerkleTreePathStepBranch.class));
        Assertions.assertThrows(JsonMappingException.class, () -> objectMapper.readValue("[\"AABBGG\"]", MerkleTreePathStepBranch.class));
    }
}
