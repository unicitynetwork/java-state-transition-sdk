package org.unicitylabs.sdk;

import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.IAggregatorClient;
import org.unicitylabs.sdk.api.LeafValue;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.api.SubmitCommitmentStatus;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTree;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreeRootNode;
import org.unicitylabs.sdk.transaction.InclusionProof;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

public class TestAggregatorClient implements IAggregatorClient {

  private final SparseMerkleTree tree = new SparseMerkleTree(HashAlgorithm.SHA256);
  private final HashMap<RequestId, Map.Entry<Authenticator, DataHash>> requests = new HashMap<>();


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
  public CompletableFuture<InclusionProof> getInclusionProof(RequestId requestId) {
    Entry<Authenticator, DataHash> entry = requests.get(requestId);
    SparseMerkleTreeRootNode root = tree.calculateRoot();
    return CompletableFuture.completedFuture(
        new InclusionProof(
            root.getPath(requestId.toBitString().toBigInteger()),
            entry.getKey(),
            entry.getValue())
    );
  }

  @Override
  public CompletableFuture<Long> getBlockHeight() {
    return CompletableFuture.completedFuture(1L);
  }
}
