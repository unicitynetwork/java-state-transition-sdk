package org.unicitylabs.sdk.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.unicitylabs.sdk.api.bft.RootTrustBase;
import org.unicitylabs.sdk.api.bft.RootTrustBaseUtils;
import org.unicitylabs.sdk.api.bft.ShardId;
import org.unicitylabs.sdk.api.bft.UnicityCertificate;
import org.unicitylabs.sdk.api.bft.UnicityCertificateUtils;
import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.crypto.secp256k1.SigningService;
import org.unicitylabs.sdk.predicate.builtin.SignaturePredicate;
import org.unicitylabs.sdk.predicate.builtin.SignaturePredicateUnlockScript;
import org.unicitylabs.sdk.predicate.verification.PredicateVerifierService;
import org.unicitylabs.sdk.smt.radix.SparseMerkleTree;
import org.unicitylabs.sdk.smt.radix.SparseMerkleTreeRootNode;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.transaction.verification.InclusionProofVerificationRule;
import org.unicitylabs.sdk.transaction.verification.InclusionProofVerificationStatus;
import org.unicitylabs.sdk.util.HexConverter;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InclusionProofTest {

  static final long REFERENCE_TIME = 1755000000L;

  MintTransaction transaction;
  PredicateVerifierService predicateVerifier;
  StateId stateId;
  InclusionCertificate inclusionCertificate;
  CertificationData certificationData;
  RootTrustBase trustBase;
  UnicityCertificate unicityCertificate;

  @BeforeAll
  public void createMerkleTreePath() throws Exception {
    SigningService signingService = new SigningService(
            HexConverter.decode("0000000000000000000000000000000000000000000000000000000000000001"));


    transaction = MintTransaction.create(
            NetworkId.LOCAL,
            SignaturePredicate.fromSigningService(signingService)
    );

    certificationData = CertificationData.fromMintTransaction(transaction);
    stateId = StateId.fromCertificationData(certificationData);

    SparseMerkleTree smt = new SparseMerkleTree(HashAlgorithm.SHA256);
    smt.addLeaf(stateId.getData(),
            LeafValue.calculate(certificationData.getTransactionHash(), REFERENCE_TIME).getData());

    SparseMerkleTreeRootNode root = smt.calculateRoot();
    inclusionCertificate = InclusionCertificate.create(root, stateId.getData());
    // Reuse user signing service as unicity certificate signing service.
    trustBase = RootTrustBaseUtils.generateRootTrustBase(signingService.getPublicKey());
    unicityCertificate = UnicityCertificateUtils.generateCertificate(signingService, root.getHash());
    predicateVerifier = PredicateVerifierService.create();
  }

  @Test
  public void testCborSerialization() {
    InclusionProof inclusionProof = new InclusionProof(
            certificationData,
            REFERENCE_TIME,
            inclusionCertificate,
            unicityCertificate
    );

    Assertions.assertEquals(inclusionProof, InclusionProof.fromCbor(inclusionProof.toCbor()));
  }

  @Test
  public void testStructure() {
    Assertions.assertThrows(NullPointerException.class,
            () -> new InclusionProof(
                    this.certificationData,
                    REFERENCE_TIME,
                    this.inclusionCertificate,
                    null
            )
    );
    Assertions.assertInstanceOf(InclusionProof.class,
            new InclusionProof(
                    this.certificationData,
                    REFERENCE_TIME,
                    this.inclusionCertificate,
                    this.unicityCertificate
            )
    );
    Assertions.assertInstanceOf(InclusionProof.class,
            new InclusionProof(
                    null,
                    null,
                    this.inclusionCertificate,
                    this.unicityCertificate
            )
    );
  }

  @Test
  public void testItVerifies() {
    InclusionProof inclusionProof = new InclusionProof(
            this.certificationData,
            REFERENCE_TIME,
            this.inclusionCertificate,
            this.unicityCertificate
    );
    Assertions.assertEquals(
            InclusionProofVerificationStatus.OK,
            InclusionProofVerificationRule.verify(
                    this.trustBase,
                    this.predicateVerifier,
                    inclusionProof,
                    this.transaction,
                    REFERENCE_TIME
            ).getStatus()
    );

    InclusionProof invalidTransactionHashInclusionProof = new InclusionProof(
            new CertificationData(
                    this.certificationData.getLockScript(),
                    this.certificationData.getSourceStateHash(),
                    DataHash.fromImprint(
                            HexConverter.decode("00000000000000000000000000000000000000000000000000000000000000000001")
                    ),
                    this.certificationData.getUnlockScript()
            ),
            REFERENCE_TIME,
            this.inclusionCertificate,
            this.unicityCertificate
    );

    Assertions.assertEquals(
            InclusionProofVerificationStatus.TRANSACTION_HASH_MISMATCH,
            InclusionProofVerificationRule.verify(
                    this.trustBase,
                    this.predicateVerifier,
                    invalidTransactionHashInclusionProof,
                    this.transaction,
                    REFERENCE_TIME
            ).getStatus()
    );
  }

  @Test
  public void testItNotAuthenticated() {
    InclusionProof invalidInclusionProof = new InclusionProof(
            new CertificationData(
                    this.certificationData.getLockScript(),
                    this.certificationData.getSourceStateHash(),
                    this.certificationData.getTransactionHash(),
                    SignaturePredicateUnlockScript.create(
                            this.transaction,
                            new SigningService(SigningService.generatePrivateKey())
                    ).encode()
            ),
            REFERENCE_TIME,
            this.inclusionCertificate,
            this.unicityCertificate
    );

    Assertions.assertEquals(
            InclusionProofVerificationStatus.NOT_AUTHENTICATED,
            InclusionProofVerificationRule.verify(
                    this.trustBase,
                    this.predicateVerifier,
                    invalidInclusionProof,
                    this.transaction,
                    REFERENCE_TIME
            ).getStatus()
    );
  }

  @Test
  public void testItFailsWithShardIdMismatch() {
    // 1-byte shard id whose first byte doesn't match the state id's first byte. The shard check
    // runs before the trust base check, so the signing service used for the new certificate's seal
    // is irrelevant — reuse the test's fixed key.
    byte mismatchingByte = (byte) (this.stateId.getData()[0] ^ 0xFF);
    ShardId mismatchingShardId = ShardId.decode(new byte[]{mismatchingByte, (byte) 0x80});
    DataHash rootHash = new DataHash(HashAlgorithm.SHA256,
            this.unicityCertificate.getInputRecord().getHash());
    SigningService signingService = SigningService.generate();
    UnicityCertificate mismatchingCertificate = UnicityCertificateUtils.generateCertificate(
            signingService,
            rootHash,
            mismatchingShardId
    );

    InclusionProof inclusionProof = new InclusionProof(
            this.certificationData,
            REFERENCE_TIME,
            this.inclusionCertificate,
            mismatchingCertificate
    );

    Assertions.assertEquals(
            InclusionProofVerificationStatus.SHARD_ID_MISMATCH,
            InclusionProofVerificationRule.verify(
                    RootTrustBaseUtils.generateRootTrustBase(signingService.getPublicKey()),
                    this.predicateVerifier,
                    inclusionProof,
                    this.transaction,
                    REFERENCE_TIME
            ).getStatus()
    );
  }

  @Test
  public void testVerificationFailsWithWrongReferenceTime() {
    InclusionProof inclusionProof = new InclusionProof(
            this.certificationData,
            REFERENCE_TIME,
            this.inclusionCertificate,
            this.unicityCertificate
    );

    Assertions.assertEquals(
            InclusionProofVerificationStatus.PATH_INVALID,
            InclusionProofVerificationRule.verify(
                    this.trustBase,
                    this.predicateVerifier,
                    inclusionProof,
                    this.transaction,
                    REFERENCE_TIME + 1
            ).getStatus()
    );
  }

  @Test
  public void testVerificationFailsWithInvalidTrustbase() {
    InclusionProof inclusionProof = new InclusionProof(
            this.certificationData,
            REFERENCE_TIME,
            this.inclusionCertificate,
            this.unicityCertificate
    );

    Assertions.assertEquals(
            InclusionProofVerificationStatus.INVALID_TRUSTBASE,
            InclusionProofVerificationRule.verify(
                    RootTrustBaseUtils.generateRootTrustBase(
                            HexConverter.decode("020000000000000000000000000000000000000000000000000000000000000001")
                    ),
                    this.predicateVerifier,
                    inclusionProof,
                    this.transaction,
                    REFERENCE_TIME
            ).getStatus()
    );
  }
}
