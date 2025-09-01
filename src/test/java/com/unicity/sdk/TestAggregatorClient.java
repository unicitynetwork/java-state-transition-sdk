package com.unicity.sdk;

import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.IAggregatorClient;
import com.unicity.sdk.api.LeafValue;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.api.SubmitCommitmentResponse;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.mtree.BranchExistsException;
import com.unicity.sdk.mtree.LeafOutOfBoundsException;
import com.unicity.sdk.mtree.plain.SparseMerkleTree;
import com.unicity.sdk.mtree.plain.SparseMerkleTreeRootNode;
import com.unicity.sdk.transaction.InclusionProof;
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
      throw new RuntimeException(e);
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
