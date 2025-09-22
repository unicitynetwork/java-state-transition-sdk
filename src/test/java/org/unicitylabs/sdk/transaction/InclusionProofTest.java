package org.unicitylabs.sdk.transaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.LeafValue;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTree;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.util.HexConverter;
import org.unicitylabs.sdk.utils.UnicityCertificateUtils;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InclusionProofTest {

  RequestId requestId;
  DataHash transactionHash;
  SparseMerkleTreePath merkleTreePath;
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
    InclusionProof inclusionProof = new InclusionProof(
        merkleTreePath,
        authenticator,
        transactionHash,
        UnicityCertificateUtils.generateCertificate()
    );
    Assertions.assertEquals(inclusionProof, UnicityObjectMapper.JSON.readValue(
        UnicityObjectMapper.JSON.writeValueAsString(inclusionProof), InclusionProof.class));
  }

  @Test
  public void testStructure() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new InclusionProof(
            merkleTreePath,
            authenticator,
            null,
            UnicityCertificateUtils.generateCertificate()
        )
    );
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new InclusionProof(
            merkleTreePath,
            null,
            transactionHash,
            UnicityCertificateUtils.generateCertificate()
        )
    );
    Assertions.assertThrows(NullPointerException.class,
        () -> new InclusionProof(
            null,
            authenticator,
            transactionHash,
            UnicityCertificateUtils.generateCertificate()
        )
    );
    Assertions.assertThrows(NullPointerException.class,
        () -> new InclusionProof(
            merkleTreePath,
            authenticator,
            transactionHash,
            null
        )
    );
    Assertions.assertInstanceOf(InclusionProof.class,
        new InclusionProof(
            merkleTreePath,
            authenticator,
            transactionHash,
            UnicityCertificateUtils.generateCertificate()
        )
    );
    Assertions.assertInstanceOf(InclusionProof.class,
        new InclusionProof(
            merkleTreePath,
            null,
            null,
            UnicityCertificateUtils.generateCertificate()
        )
    );
  }

  @Test
  public void testItVerifies() {
    InclusionProof inclusionProof = new InclusionProof(
        merkleTreePath,
        authenticator,
        transactionHash,
        UnicityCertificateUtils.generateCertificate()
    );
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
        ),
        UnicityCertificateUtils.generateCertificate()
    );

    Assertions.assertEquals(InclusionProofVerificationStatus.NOT_AUTHENTICATED,
        invalidInclusionProof.verify(requestId));
  }
}
