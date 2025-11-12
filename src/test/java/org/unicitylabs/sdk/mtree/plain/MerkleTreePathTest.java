package org.unicitylabs.sdk.mtree.plain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.MerkleTreePathVerificationResult;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.util.HexConverter;
import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MerkleTreePathTest {

  @Test
  public void testConstructorThrowsOnNullArguments() {
    Exception exception = assertThrows(NullPointerException.class,
        () -> new SparseMerkleTreePath(null, null)
    );
    assertEquals("rootHash cannot be null", exception.getMessage());
    exception = assertThrows(NullPointerException.class,
        () -> new SparseMerkleTreePath(new DataHash(HashAlgorithm.SHA256, new byte[32]), null)
    );
    assertEquals("steps cannot be null", exception.getMessage());
  }

  @Test
  public void testJsonSerialization() throws JsonProcessingException {
    ObjectMapper objectMapper = UnicityObjectMapper.JSON;

    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"root\":\"00000000\"}", SparseMerkleTreePath.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"steps\":[]}", SparseMerkleTreePath.class));
    Assertions.assertThrows(ValueInstantiationException.class,
        () -> objectMapper.readValue("{\"root\": null, \"steps\":[]}", SparseMerkleTreePath.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"root\": \"asd\", \"steps\":[]}",
            SparseMerkleTreePath.class));
    Assertions.assertThrows(JsonMappingException.class, () -> objectMapper.readValue(
        "{\"root\": \"000001\", \"steps\":[{\"sibling\": null, \"branch\": [\"asd\"], \"path\": \"5\"}]}",
        SparseMerkleTreePath.class));

    SparseMerkleTreePath path = new SparseMerkleTreePath(
        new DataHash(HashAlgorithm.SHA256, new byte[32]),
        List.of(
            new SparseMerkleTreePathStep(
                BigInteger.ONE,
                new DataHash(HashAlgorithm.SHA384, new byte[5]).getImprint()
            )
        ));

    Assertions.assertEquals(path,
        objectMapper.readValue(objectMapper.writeValueAsString(path), SparseMerkleTreePath.class));
  }

  @Test
  public void testShouldVerifyInclusionProof() {
    SparseMerkleTreePath path = new SparseMerkleTreePath(
        DataHash.fromImprint(
            HexConverter.decode(
                "0000e9748bbd0c45fc357ffe7c221c7db1ef02f589680d8b0a370b48a669435bde13"
            )
        ),
        List.of(
            new SparseMerkleTreePathStep(
                BigInteger.valueOf(69),
                HexConverter.decode("76616c756535")
            ),
            new SparseMerkleTreePathStep(
                BigInteger.valueOf(4),
                HexConverter.decode(
                    "8471f8ea3c9a0e50627df4c72d9bd5affbdc12050ee7f4250974ed64949f3b0f"
                )
            ),
            new SparseMerkleTreePathStep(
                BigInteger.valueOf(1),
                HexConverter.decode(
                    "66507538ce0fae31018cfc7b01841b5308e7e44306445710acee947ec4a4b2cd"
                )
            )
        )
    );

    Assertions.assertEquals(new MerkleTreePathVerificationResult(true, true),
        path.verify(BigInteger.valueOf(0b100010100)));
    Assertions.assertEquals(new MerkleTreePathVerificationResult(true, false),
        path.verify(BigInteger.valueOf(0b111)));
  }

  @Test
  public void testEmptyPathVerification() throws JsonProcessingException {
    SparseMerkleTreePath path = UnicityObjectMapper.JSON.readValue(
        "{\"root\":\"00001e54402898172f2948615fb17627733abbd120a85381c624ad060d28321be672\",\"steps\":[{\"path\":\"1\",\"data\":null},{\"path\":\"1\",\"data\":null}]}",
        SparseMerkleTreePath.class);

    MerkleTreePathVerificationResult result = path.verify(BigInteger.valueOf(101));
    Assertions.assertTrue(result.isPathValid());
    Assertions.assertFalse(result.isPathIncluded());
  }
}
