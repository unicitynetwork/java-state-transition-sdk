package org.unicitylabs.sdk.mtree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePathStep;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.util.HexConverter;
import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MerkleTreePathTest {

  @Test
  public void testConstructorThrowsOnNullArguments() {
    Exception exception = assertThrows(NullPointerException.class, () -> {
      new SparseMerkleTreePath(null, null);
    });
    assertEquals("rootHash cannot be null", exception.getMessage());
    exception = assertThrows(NullPointerException.class, () -> {
      new SparseMerkleTreePath(new DataHash(HashAlgorithm.SHA256, new byte[32]), null);
    });
    assertEquals("steps cannot be null", exception.getMessage());
  }

  @Test
  public void testJsonSerialization() throws JsonProcessingException {
    ObjectMapper objectMapper = UnicityObjectMapper.JSON;

    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"root\":\"00000000\"}", SparseMerkleTreePath.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"steps\":[]}", SparseMerkleTreePath.class));
    Assertions.assertThrows(NullPointerException.class,
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
                new SparseMerkleTreePathStep.Branch(
                    new DataHash(HashAlgorithm.SHA384, new byte[5]).getImprint()),
                new SparseMerkleTreePathStep.Branch(new byte[3])
            )
        ));

    Assertions.assertEquals(path,
        objectMapper.readValue(objectMapper.writeValueAsString(path), SparseMerkleTreePath.class));
  }

  @Test
  public void testShouldVerifyInclusionProof() {
    SparseMerkleTreePath path = new SparseMerkleTreePath(
        DataHash.fromImprint(HexConverter.decode(
            "00001fd5fffc41e26f249d04e435b71dbe86d079711131671ed54431a5e117291b42")),
        List.of(
            new SparseMerkleTreePathStep(
                BigInteger.valueOf(16),
                new SparseMerkleTreePathStep.Branch(
                    HexConverter.decode(
                        "6c5ad75422175395b4b63390e9dea5d0a39017f4750b78cc4b89ac6451265345")),
                new SparseMerkleTreePathStep.Branch(
                    HexConverter.decode("76616c75653030303030303030"))),
            new SparseMerkleTreePathStep(
                BigInteger.valueOf(4),
                new SparseMerkleTreePathStep.Branch(
                    HexConverter.decode(
                        "ed454d5723b169c882ec9ad5e7f73b2bb804ec1a3cf1dd0eb24faa833ffd9eef")),
                new SparseMerkleTreePathStep.Branch(null)),
            new SparseMerkleTreePathStep(
                BigInteger.valueOf(2),
                new SparseMerkleTreePathStep.Branch(
                    HexConverter.decode(
                        "e61c02aab33310b526224da3f2ed765ecea0e9a7ac5a307bf7736cca38d00067")),
                new SparseMerkleTreePathStep.Branch(null)),
            new SparseMerkleTreePathStep(
                BigInteger.valueOf(2),
                new SparseMerkleTreePathStep.Branch(
                    HexConverter.decode(
                        "be9ef65f6d3b6057acc7668fcbb23f9a5ae573d21bd5ebc3d9f4eee3a3c706a3")),
                new SparseMerkleTreePathStep.Branch(null))
        )
    );

    Assertions.assertEquals(new MerkleTreePathVerificationResult(true, true),
        path.verify(BigInteger.valueOf(0b100000000)));
    Assertions.assertEquals(new MerkleTreePathVerificationResult(true, false),
        path.verify(BigInteger.valueOf(0b100)));
  }

  @Test
  public void testShouldVerifyNonInclusionProof() {
    SparseMerkleTreePath path = new SparseMerkleTreePath(
        DataHash.fromImprint(HexConverter.decode(
            "000096a296d224f285c67bee93c30f8a309157f0daa35dc5b87e410b78630a09cfc7")),
        List.of(
            new SparseMerkleTreePathStep(
                BigInteger.valueOf(16),
                new SparseMerkleTreePathStep.Branch(HexConverter.decode(
                    "00006c5ad75422175395b4b63390e9dea5d0a39017f4750b78cc4b89ac6451265345")),
                new SparseMerkleTreePathStep.Branch(
                    HexConverter.decode("76616c75653030303030303030"))),
            new SparseMerkleTreePathStep(BigInteger.valueOf(4), null, null)
        )
    );

    Assertions.assertEquals(new MerkleTreePathVerificationResult(true, false),
        path.verify(BigInteger.valueOf(0b1000000)));
  }


}
