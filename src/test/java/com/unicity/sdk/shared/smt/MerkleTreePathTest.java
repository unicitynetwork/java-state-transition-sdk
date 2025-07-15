package com.unicity.sdk.shared.smt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.smt.path.MerkleTreePath;
import com.unicity.sdk.shared.smt.path.MerkleTreePathStep;
import com.unicity.sdk.shared.smt.path.MerkleTreePathStepBranch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MerkleTreePathTest {
    @Test
    public void testConstructorThrowsOnNullArguments() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new MerkleTreePath(null, null);
        });
        assertEquals("Invalid root hash: null", exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () -> {
            new MerkleTreePath(new DataHash(HashAlgorithm.SHA256, new byte[32]), null);
        });
        assertEquals("Invalid steps: null", exception.getMessage());
    }

    @Test
    public void testMerkleTreePathStepJsonSerialization() throws JsonProcessingException {
        ObjectMapper objectMapper = UnicityObjectMapper.JSON;

        Assertions.assertThrows(JsonMappingException.class, () -> objectMapper.readValue("{\"root\":\"00000000\"}", MerkleTreePath.class));
        Assertions.assertThrows(JsonMappingException.class, () -> objectMapper.readValue("{\"steps\":[]}", MerkleTreePath.class));
        Assertions.assertThrows(JsonMappingException.class, () -> objectMapper.readValue("{\"root\": null, \"steps\":[]}", MerkleTreePath.class));
        Assertions.assertThrows(JsonMappingException.class, () -> objectMapper.readValue("{\"root\": \"asd\", \"steps\":[]}", MerkleTreePath.class));
        Assertions.assertThrows(JsonMappingException.class, () -> objectMapper.readValue("{\"root\": \"000001\", \"steps\":[{\"sibling\": null, \"branch\": [\"asd\"], \"path\": \"5\"}]}", MerkleTreePath.class));



        MerkleTreePath path = new MerkleTreePath(new DataHash(HashAlgorithm.SHA256, new byte[32]), List.of(
                new MerkleTreePathStep(
                        BigInteger.ONE,
                        new DataHash(HashAlgorithm.SHA384, new byte[5]),
                        new MerkleTreePathStepBranch(new byte[3])
                )
        ));
        Assertions.assertEquals(path, objectMapper.readValue(objectMapper.writeValueAsString(path), MerkleTreePath.class));
    }
}
