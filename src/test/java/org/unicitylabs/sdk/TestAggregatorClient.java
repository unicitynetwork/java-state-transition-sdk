package org.unicitylabs.sdk;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.IAggregatorClient;
import org.unicitylabs.sdk.api.InclusionProofResponse;
import org.unicitylabs.sdk.api.LeafValue;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.api.SubmitCommitmentStatus;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTree;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreeRootNode;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.transaction.InclusionProof;
import org.unicitylabs.sdk.bft.UnicityCertificateUtils;
import org.unicitylabs.sdk.transaction.InclusionProofFixture;
import org.unicitylabs.sdk.utils.TestUtils;

public class TestAggregatorClient implements IAggregatorClient {

  private final SparseMerkleTree tree = new SparseMerkleTree(HashAlgorithm.SHA256);
  private final HashMap<RequestId, Map.Entry<Authenticator, DataHash>> requests = new HashMap<>();
  private final SigningService signingService;

  public TestAggregatorClient(SigningService signingService) {
    Objects.requireNonNull(signingService, "Signing service cannot be null");
    this.signingService = signingService;
  }


  @Override
  public CompletableFuture<SubmitCommitmentResponse> submitCommitment(RequestId requestId,
      DataHash transactionHash, Authenticator authenticator) {

    try {
      tree.addLeaf(
          requestId.toBitString().toBigInteger(),
          LeafValue.create(authenticator, transactionHash).getBytes()
      );

      requests.put(requestId, new AbstractMap.SimpleEntry<>(authenticator, transactionHash));

      return CompletableFuture.completedFuture(
          new SubmitCommitmentResponse(SubmitCommitmentStatus.SUCCESS)
      );
    } catch (Exception e) {
      throw new RuntimeException("Aggregator commitment failed", e);
    }
  }

  @Override
  public CompletableFuture<InclusionProofResponse> getInclusionProof(RequestId requestId) {
    Entry<Authenticator, DataHash> entry = requests.get(requestId);
    SparseMerkleTreeRootNode root = tree.calculateRoot();
    return CompletableFuture.completedFuture(
        new InclusionProofResponse(
            InclusionProofFixture.create(
                root.getPath(requestId.toBitString().toBigInteger()),
                entry.getKey(),
                entry.getValue(),
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
