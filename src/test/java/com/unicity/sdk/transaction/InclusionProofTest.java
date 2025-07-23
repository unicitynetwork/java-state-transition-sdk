package com.unicity.sdk.transaction;

import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.LeafValue;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.smt.SparseMerkleTree;
import com.unicity.sdk.smt.path.MerkleTreePath;
import com.unicity.sdk.util.HexConverter;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InclusionProofTest {

  RequestId requestId;
  DataHash transactionHash;
  MerkleTreePath merkleTreePath;
  Authenticator authenticator;

  @BeforeAll
  public void createMerkleTreePath() throws Exception {
    SigningService signingService = new SigningService(
        HexConverter.decode("0000000000000000000000000000000000000000000000000000000000000001"));

    transactionHash = new DataHash(HashAlgorithm.SHA256, new byte[32]);
    authenticator = Authenticator.create(signingService, transactionHash,
        new DataHash(HashAlgorithm.SHA256, new byte[32]));

    LeafValue leaf = LeafValue.create(authenticator, transactionHash);
    requestId = RequestId.create(signingService.getPublicKey(), authenticator.getStateHash());

    SparseMerkleTree smt = new SparseMerkleTree(HashAlgorithm.SHA256);
    smt.addLeaf(requestId.toBitString().toBigInteger(), leaf.getBytes());

    merkleTreePath = smt.calculateRoot().getPath(requestId.toBitString().toBigInteger());
  }

  @Test
  public void testJsonSerialization() throws Exception {
    InclusionProof inclusionProof = new InclusionProof(merkleTreePath, authenticator,
        transactionHash);
    Assertions.assertEquals(inclusionProof, UnicityObjectMapper.JSON.readValue(
        UnicityObjectMapper.JSON.writeValueAsString(inclusionProof), InclusionProof.class));
  }

  @Test
  public void testStructure() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new InclusionProof(merkleTreePath, authenticator, null));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new InclusionProof(merkleTreePath, null, transactionHash));
    Assertions.assertInstanceOf(InclusionProof.class,
        new InclusionProof(merkleTreePath, authenticator, transactionHash));
    Assertions.assertInstanceOf(InclusionProof.class,
        new InclusionProof(merkleTreePath, null, null));
  }

  @Test
  public void testItVerifies() {
    InclusionProof inclusionProof = new InclusionProof(merkleTreePath, authenticator,
        transactionHash);
    Assertions.assertEquals(InclusionProofVerificationStatus.OK, inclusionProof.verify(requestId));
    Assertions.assertEquals(InclusionProofVerificationStatus.PATH_NOT_INCLUDED,
        inclusionProof.verify(
            RequestId.create(new byte[32], new DataHash(HashAlgorithm.SHA256, new byte[32]))));

    InclusionProof invalidInclusionProof = new InclusionProof(
        merkleTreePath,
        authenticator,
        new DataHash(
            HashAlgorithm.SHA224,
            HexConverter.decode("FF000000000000000000000000000000000000000000000000000000000000FF")
        )
    );

    Assertions.assertEquals(InclusionProofVerificationStatus.NOT_AUTHENTICATED,
        invalidInclusionProof.verify(requestId));
  }
}
