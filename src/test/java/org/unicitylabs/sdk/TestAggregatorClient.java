package org.unicitylabs.sdk;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.unicitylabs.sdk.api.AggregatorClient;
import org.unicitylabs.sdk.api.CertificationData;
import org.unicitylabs.sdk.api.CertificationResponse;
import org.unicitylabs.sdk.api.CertificationStatus;
import org.unicitylabs.sdk.api.InclusionProofResponse;
import org.unicitylabs.sdk.api.StateId;
import org.unicitylabs.sdk.bft.UnicityCertificateUtils;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTree;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreeRootNode;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.transaction.InclusionProofFixture;

public class TestAggregatorClient implements AggregatorClient {

  private final SparseMerkleTree tree = new SparseMerkleTree(HashAlgorithm.SHA256);
  private final HashMap<StateId, CertificationData> requests = new HashMap<>();
  private final SigningService signingService;

  public TestAggregatorClient(SigningService signingService) {
    Objects.requireNonNull(signingService, "Signing service cannot be null");
    this.signingService = signingService;
  }


  @Override
  public CompletableFuture<CertificationResponse> submitCertificationRequest(
      CertificationData certificationData,
      boolean receipt
  ) {
    // TODO: Add checks if everything is valid
    try {
      StateId stateId = certificationData.calculateStateId();

      tree.addLeaf(
          stateId.toBitString().toBigInteger(),
          certificationData.calculateLeafValue().getImprint()
      );

      requests.put(stateId, certificationData);

      return CompletableFuture.completedFuture(CertificationResponse.create(CertificationStatus.SUCCESS));
    } catch (Exception e) {
      throw new RuntimeException("Aggregator commitment failed", e);
    }
  }

  @Override
  public CompletableFuture<InclusionProofResponse> getInclusionProof(StateId stateId) {
    CertificationData certificationData = requests.get(stateId);
    SparseMerkleTreeRootNode root = tree.calculateRoot();
    return CompletableFuture.completedFuture(
        new InclusionProofResponse(
            InclusionProofFixture.create(
                root.getPath(stateId.toBitString().toBigInteger()),
                certificationData,
                UnicityCertificateUtils.generateCertificate(signingService, root.getRootHash())
            )
        )
    );
  }

  @Override
  public CompletableFuture<Long> getBlockHeight() {
    return CompletableFuture.completedFuture(1L);
  }
}
