package com.unicity.sdk.smt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.smt.FinalizedLeafBranch;
import com.unicity.sdk.smt.path.MerkleTreePathStep;
import com.unicity.sdk.smt.path.MerkleTreePathStepBranch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MerkleTreePathStepTest {
    @Test
    public void testConstructorThrowsOnNullArguments() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new MerkleTreePathStep(null, null, (FinalizedLeafBranch) null);
        });
        assertEquals("Invalid path: null", exception.getMessage());
    }

    @Test
    public void testJsonSerialization() throws JsonProcessingException {
        ObjectMapper objectMapper = UnicityObjectMapper.JSON;

        JsonMappingException exception = Assertions.assertThrows(JsonMappingException.class, () -> objectMapper.readValue("{\"path\":\"asd\",\"sibling\":null,\"branch\":null}", MerkleTreePathStep.class));
        exception = Assertions.assertThrows(JsonMappingException.class, () -> objectMapper.readValue("{\"path\":[],\"sibling\":null,\"branch\":null}", MerkleTreePathStep.class));
        exception = Assertions.assertThrows(JsonMappingException.class, () -> objectMapper.readValue("{\"sibling\":null,\"branch\":null}", MerkleTreePathStep.class));
        exception = Assertions.assertThrows(JsonMappingException.class, () -> objectMapper.readValue("{\"path\":\"5\",\"branch\":null}", MerkleTreePathStep.class));
        exception = Assertions.assertThrows(JsonMappingException.class, () -> objectMapper.readValue("{\"path\":\"5\",\"sibling\":null,\"branch\":\"asd\"}", MerkleTreePathStep.class));

        MerkleTreePathStep step = new MerkleTreePathStep(
                BigInteger.ONE,
                new DataHash(HashAlgorithm.SHA384, new byte[5]),
                new MerkleTreePathStepBranch(new byte[3])
        );
        Assertions.assertEquals(step, objectMapper.readValue(objectMapper.writeValueAsString(step), MerkleTreePathStep.class));

    }
}
