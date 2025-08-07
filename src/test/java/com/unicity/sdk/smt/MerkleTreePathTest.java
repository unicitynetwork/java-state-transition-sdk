package com.unicity.sdk.smt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.smt.path.MerkleTreePath;
import com.unicity.sdk.smt.path.MerkleTreePathStep;
import com.unicity.sdk.smt.path.MerkleTreePathStepBranch;
import com.unicity.sdk.smt.path.MerkleTreePathVerificationResult;
import com.unicity.sdk.util.HexConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MerkleTreePathTest {

  @Test
  public void testConstructorThrowsOnNullArguments() {
    Exception exception = assertThrows(NullPointerException.class, () -> {
      new MerkleTreePath(null, null);
    });
    assertEquals("rootHash cannot be null", exception.getMessage());
    exception = assertThrows(NullPointerException.class, () -> {
      new MerkleTreePath(new DataHash(HashAlgorithm.SHA256, new byte[32]), null);
    });
    assertEquals("steps cannot be null", exception.getMessage());
  }

  @Test
  public void testJsonSerialization() throws JsonProcessingException {
    ObjectMapper objectMapper = UnicityObjectMapper.JSON;

    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"root\":\"00000000\"}", MerkleTreePath.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"steps\":[]}", MerkleTreePath.class));
    Assertions.assertThrows(NullPointerException.class,
        () -> objectMapper.readValue("{\"root\": null, \"steps\":[]}", MerkleTreePath.class));
    Assertions.assertThrows(JsonMappingException.class,
        () -> objectMapper.readValue("{\"root\": \"asd\", \"steps\":[]}", MerkleTreePath.class));
    Assertions.assertThrows(JsonMappingException.class, () -> objectMapper.readValue(
        "{\"root\": \"000001\", \"steps\":[{\"sibling\": null, \"branch\": [\"asd\"], \"path\": \"5\"}]}",
        MerkleTreePath.class));

    MerkleTreePath path = new MerkleTreePath(new DataHash(HashAlgorithm.SHA256, new byte[32]),
        List.of(
            new MerkleTreePathStep(
                BigInteger.ONE,
                new DataHash(HashAlgorithm.SHA384, new byte[5]),
                new MerkleTreePathStepBranch(new byte[3])
            )
        ));
    Assertions.assertEquals(path,
        objectMapper.readValue(objectMapper.writeValueAsString(path), MerkleTreePath.class));
  }

  @Test
  public void testShouldVerifyInclusionProof() {
    MerkleTreePath path = new MerkleTreePath(
        DataHash.fromImprint(HexConverter.decode(
            "00001fd5fffc41e26f249d04e435b71dbe86d079711131671ed54431a5e117291b42")),
        List.of(
            new MerkleTreePathStep(
                BigInteger.valueOf(16),
                DataHash.fromImprint(HexConverter.decode(
                    "00006c5ad75422175395b4b63390e9dea5d0a39017f4750b78cc4b89ac6451265345")),
                new MerkleTreePathStepBranch(HexConverter.decode("76616c75653030303030303030"))),
            new MerkleTreePathStep(
                BigInteger.valueOf(4),
                DataHash.fromImprint(HexConverter.decode(
                    "0000ed454d5723b169c882ec9ad5e7f73b2bb804ec1a3cf1dd0eb24faa833ffd9eef")),
                new MerkleTreePathStepBranch(null)),
            new MerkleTreePathStep(
                BigInteger.valueOf(2),
                DataHash.fromImprint(HexConverter.decode(
                    "0000e61c02aab33310b526224da3f2ed765ecea0e9a7ac5a307bf7736cca38d00067")),
                new MerkleTreePathStepBranch(null)),
            new MerkleTreePathStep(
                BigInteger.valueOf(2),
                DataHash.fromImprint(HexConverter.decode(
                    "0000be9ef65f6d3b6057acc7668fcbb23f9a5ae573d21bd5ebc3d9f4eee3a3c706a3")),
                new MerkleTreePathStepBranch(null))
        )
    );

    Assertions.assertEquals(new MerkleTreePathVerificationResult(true, true),
        path.verify(BigInteger.valueOf(0b100000000)));
    Assertions.assertEquals(new MerkleTreePathVerificationResult(true, false),
        path.verify(BigInteger.valueOf(0b100)));
  }

  @Test
  public void testShouldVerifyNonInclusionProof() {
    MerkleTreePath path = new MerkleTreePath(
        DataHash.fromImprint(HexConverter.decode(
            "000096a296d224f285c67bee93c30f8a309157f0daa35dc5b87e410b78630a09cfc7")),
        List.of(
            new MerkleTreePathStep(
                BigInteger.valueOf(16),
                DataHash.fromImprint(HexConverter.decode(
                    "00006c5ad75422175395b4b63390e9dea5d0a39017f4750b78cc4b89ac6451265345")),
                new MerkleTreePathStepBranch(HexConverter.decode("76616c75653030303030303030"))),
            new MerkleTreePathStep(BigInteger.valueOf(4), (DataHash) null, null)
        )
    );

    Assertions.assertEquals(new MerkleTreePathVerificationResult(true, false),
        path.verify(BigInteger.valueOf(0b1000000)));
  }


}
