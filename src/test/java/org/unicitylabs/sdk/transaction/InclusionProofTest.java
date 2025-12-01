package org.unicitylabs.sdk.transaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.unicitylabs.sdk.api.CertificationData;
import org.unicitylabs.sdk.api.StateId;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.bft.RootTrustBaseUtils;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.bft.UnicityCertificateUtils;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTree;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.util.HexConverter;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InclusionProofTest {

  StateId stateId;
  SparseMerkleTreePath merkleTreePath;
  CertificationData certificationData;
  RootTrustBase trustBase;
  UnicityCertificate unicityCertificate;

  @BeforeAll
  public void createMerkleTreePath() throws Exception {
    SigningService signingService = new SigningService(
        HexConverter.decode("0000000000000000000000000000000000000000000000000000000000000001"));

    certificationData = CertificationData.create(
        new DataHash(HashAlgorithm.SHA256, new byte[32]),
        new DataHash(HashAlgorithm.SHA256, new byte[32]),
        signingService
    );

    stateId = StateId.create(signingService.getPublicKey(), certificationData.getSourceStateHash());

    SparseMerkleTree smt = new SparseMerkleTree(HashAlgorithm.SHA256);
    smt.addLeaf(stateId.toBitString().toBigInteger(), certificationData.calculateLeafValue().getImprint());

    merkleTreePath = smt.calculateRoot().getPath(stateId.toBitString().toBigInteger());
    SigningService ucSigningService = new SigningService(SigningService.generatePrivateKey());
    trustBase = RootTrustBaseUtils.generateRootTrustBase(ucSigningService.getPublicKey());
    unicityCertificate = UnicityCertificateUtils.generateCertificate(ucSigningService,
        merkleTreePath.getRootHash());
  }

  @Test
  public void testJsonSerialization() {
    InclusionProof inclusionProof = new InclusionProof(
        merkleTreePath,
        certificationData,
        unicityCertificate
    );
    Assertions.assertEquals(inclusionProof, InclusionProof.fromJson(inclusionProof.toJson()));
  }

  @Test
  public void testCborSerialization() {
    InclusionProof inclusionProof = new InclusionProof(
        merkleTreePath,
        certificationData,
        unicityCertificate
    );

    Assertions.assertEquals(inclusionProof, InclusionProof.fromCbor(inclusionProof.toCbor()));
  }

  @Test
  public void testStructure() {
    Assertions.assertThrows(NullPointerException.class,
        () -> new InclusionProof(
            null,
            this.certificationData,
            this.unicityCertificate
        )
    );
    Assertions.assertThrows(NullPointerException.class,
        () -> new InclusionProof(
            this.merkleTreePath,
            this.certificationData,
            null
        )
    );
    Assertions.assertInstanceOf(InclusionProof.class,
        new InclusionProof(
            this.merkleTreePath,
            this.certificationData,
            this.unicityCertificate
        )
    );
    Assertions.assertInstanceOf(InclusionProof.class,
        new InclusionProof(
            this.merkleTreePath,
            null,
            this.unicityCertificate
        )
    );
  }

  @Test
  public void testItVerifies() {
    InclusionProof inclusionProof = new InclusionProof(
        this.merkleTreePath,
        this.certificationData,
        this.unicityCertificate
    );
    Assertions.assertEquals(
        InclusionProofVerificationStatus.OK,
        inclusionProof.verify(this.trustBase, this.stateId)
    );
    Assertions.assertEquals(InclusionProofVerificationStatus.PATH_NOT_INCLUDED,
        inclusionProof.verify(
            this.trustBase,
            StateId.create(new byte[32], new DataHash(HashAlgorithm.SHA256, new byte[32]))
        )
    );
  }

  @Test
  public void testItNotAuthenticated() {
    InclusionProof invalidInclusionProof = new InclusionProof(
        this.merkleTreePath,
        CertificationData.fromCbor(
            CborSerializer.encodeArray(
                CborSerializer.encodeByteString(certificationData.getPublicKey()),
                certificationData.getSourceStateHash().toCbor(),
                DataHash.fromImprint(
                        HexConverter.decode("00000000000000000000000000000000000000000000000000000000000000000001"))
                    .toCbor(),
                certificationData.getSignature().toCbor()
            )
        ),
        this.unicityCertificate
    );

    Assertions.assertEquals(
        InclusionProofVerificationStatus.NOT_AUTHENTICATED,
        invalidInclusionProof.verify(this.trustBase, this.stateId)
    );
  }

  @Test
  public void testVerificationFailsWithInvalidTrustbase() {
    InclusionProof inclusionProof = new InclusionProof(
        this.merkleTreePath,
        this.certificationData,
        this.unicityCertificate
    );

    Assertions.assertEquals(
        InclusionProofVerificationStatus.INVALID_TRUST_BASE,
        inclusionProof.verify(
            RootTrustBaseUtils.generateRootTrustBase(
                HexConverter.decode("020000000000000000000000000000000000000000000000000000000000000001")
            ),
            this.stateId
        )
    );
  }
}
