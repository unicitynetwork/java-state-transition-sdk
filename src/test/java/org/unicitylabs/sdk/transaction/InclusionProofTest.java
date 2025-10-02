package org.unicitylabs.sdk.transaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.LeafValue;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTree;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.util.HexConverter;
import org.unicitylabs.sdk.bft.RootTrustBaseUtils;
import org.unicitylabs.sdk.bft.UnicityCertificateUtils;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InclusionProofTest {

  RequestId requestId;
  DataHash transactionHash;
  SparseMerkleTreePath merkleTreePath;
  Authenticator authenticator;
  RootTrustBase trustBase;
  UnicityCertificate unicityCertificate;

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
    SigningService ucSigningService = new SigningService(SigningService.generatePrivateKey());
    trustBase = RootTrustBaseUtils.generateRootTrustBase(ucSigningService.getPublicKey());
    unicityCertificate = UnicityCertificateUtils.generateCertificate(ucSigningService, merkleTreePath.getRootHash());
  }

  @Test
  public void testJsonSerialization() throws Exception {
    InclusionProof inclusionProof = new InclusionProof(
        merkleTreePath,
        authenticator,
        transactionHash,
        unicityCertificate
    );
    Assertions.assertEquals(inclusionProof, InclusionProof.fromJson(inclusionProof.toJson()));
  }

  @Test
  public void testStructure() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new InclusionProof(
            merkleTreePath,
            authenticator,
            null,
            unicityCertificate
        )
    );
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new InclusionProof(
            merkleTreePath,
            null,
            transactionHash,
            unicityCertificate
        )
    );
    Assertions.assertThrows(NullPointerException.class,
        () -> new InclusionProof(
            null,
            authenticator,
            transactionHash,
            unicityCertificate
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
            unicityCertificate
        )
    );
    Assertions.assertInstanceOf(InclusionProof.class,
        new InclusionProof(
            merkleTreePath,
            null,
            null,
            unicityCertificate
        )
    );
  }

  @Test
  public void testItVerifies() {
    InclusionProof inclusionProof = new InclusionProof(
        this.merkleTreePath,
        this.authenticator,
        this.transactionHash,
        this.unicityCertificate
    );
    Assertions.assertEquals(
        InclusionProofVerificationStatus.OK,
        inclusionProof.verify(this.requestId, this.trustBase)
    );
    Assertions.assertEquals(InclusionProofVerificationStatus.PATH_NOT_INCLUDED,
        inclusionProof.verify(
            RequestId.create(new byte[32], new DataHash(HashAlgorithm.SHA256, new byte[32])),
            this.trustBase
        )
    );

    InclusionProof invalidInclusionProof = new InclusionProof(
        this.merkleTreePath,
        this.authenticator,
        new DataHash(
            HashAlgorithm.SHA224,
            HexConverter.decode("FF000000000000000000000000000000000000000000000000000000000000FF")
        ),
        this.unicityCertificate
    );

    Assertions.assertEquals(
        InclusionProofVerificationStatus.NOT_AUTHENTICATED,
        invalidInclusionProof.verify(this.requestId, this.trustBase)
    );
  }
}
