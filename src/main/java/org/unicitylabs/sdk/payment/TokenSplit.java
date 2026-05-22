package org.unicitylabs.sdk.payment;

import org.unicitylabs.sdk.api.NetworkId;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.payment.asset.Asset;
import org.unicitylabs.sdk.payment.asset.AssetId;
import org.unicitylabs.sdk.predicate.builtin.BurnPredicate;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.smt.BranchExistsException;
import org.unicitylabs.sdk.smt.LeafOutOfBoundsException;
import org.unicitylabs.sdk.smt.plain.SparseMerkleTree;
import org.unicitylabs.sdk.smt.plain.SparseMerkleTreeRootNode;
import org.unicitylabs.sdk.smt.sum.SparseMerkleSumTree;
import org.unicitylabs.sdk.smt.sum.SparseMerkleSumTreeRootNode;
import org.unicitylabs.sdk.transaction.Token;
import org.unicitylabs.sdk.transaction.TokenId;
import org.unicitylabs.sdk.transaction.TransferTransaction;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Utilities for creating and verifying token split proofs.
 */
public class TokenSplit {

  private static final SecureRandom RANDOM = new SecureRandom();

  private TokenSplit() {
  }

  /**
   * Create split proofs and burn transaction for the provided split token requests.
   *
   * @param token source token being split
   * @param paymentDataDeserializer payment data decoder for source token payload
   * @param requests per-output mint requests
   *
   * @return split result containing burn transaction and split tokens
   *
   * @throws LeafOutOfBoundsException if a leaf path is invalid for merkle tree insertion
   * @throws BranchExistsException if duplicate branches are inserted into a merkle tree
   */
  public static SplitResult split(
          Token token,
          PaymentDataDeserializer paymentDataDeserializer,
          List<SplitTokenRequest> requests
  ) throws LeafOutOfBoundsException, BranchExistsException {
    Objects.requireNonNull(token, "Token cannot be null");
    Objects.requireNonNull(paymentDataDeserializer, "Payment data deserializer cannot be null");
    Objects.requireNonNull(requests, "Requests cannot be null");
    byte[] paymentDataBytes = token.getGenesis().getData().orElse(null);
    if (paymentDataBytes == null) {
      throw new IllegalArgumentException("Token genesis data must be present");
    }

    NetworkId networkId = token.getGenesis().getNetworkId();
    HashMap<AssetId, SparseMerkleSumTree> trees = new HashMap<>();
    LinkedHashMap<TokenId, SplitTokenRequest> requestsByTokenId = new LinkedHashMap<>();
    for (SplitTokenRequest request : requests) {
      Objects.requireNonNull(request, "Split token request cannot be null");
      TokenId tokenId = TokenId.fromSalt(networkId, request.getSalt());
      if (requestsByTokenId.containsKey(tokenId)) {
        throw new DuplicateSplitTokenIdException(tokenId.toString());
      }
      requestsByTokenId.put(tokenId, request);

      BigInteger tokenIdPath = tokenId.toBitString().toBigInteger();
      for (Asset asset : request.getAssets()) {
        Objects.requireNonNull(asset, "Split token asset cannot be null");
        SparseMerkleSumTree tree = trees.computeIfAbsent(asset.getId(),
                v -> new SparseMerkleSumTree(HashAlgorithm.SHA256));
        tree.addLeaf(
                tokenIdPath,
                new SparseMerkleSumTree.LeafValue(asset.getId().getBytes(), asset.getValue())
        );
      }
    }

    PaymentData paymentData = paymentDataDeserializer.decode(paymentDataBytes);
    Map<AssetId, Asset> assets = paymentData.getAssets().stream()
            .collect(Collectors.toMap(
                            Asset::getId,
                            asset -> asset,
                            (a, b) -> {
                              throw new IllegalArgumentException(
                                      "Payment data contains multiple assets with the same id: " + a.getId());
                            }
                    )
            );

    if (trees.size() != assets.size()) {
      throw new IllegalArgumentException("Token and split tokens asset counts differ.");
    }

    SparseMerkleTree aggregationTree = new SparseMerkleTree(HashAlgorithm.SHA256);
    HashMap<AssetId, SparseMerkleSumTreeRootNode> assetTreeRoots = new HashMap<>();
    for (Entry<AssetId, SparseMerkleSumTree> entry : trees.entrySet()) {
      Asset tokenAsset = assets.get(entry.getKey());
      if (tokenAsset == null) {
        throw new IllegalArgumentException(String.format("Token did not contain asset %s.", entry.getKey()));
      }

      SparseMerkleSumTreeRootNode root = entry.getValue().calculateRoot();
      if (!root.getValue().equals(tokenAsset.getValue())) {
        throw new IllegalArgumentException(
                String.format(
                        "Token contained %s %s assets, but tree has %s",
                        tokenAsset.getValue(),
                        tokenAsset.getId(),
                        root.getValue()
                )
        );
      }

      assetTreeRoots.put(tokenAsset.getId(), root);
      aggregationTree.addLeaf(tokenAsset.getId().toBitString().toBigInteger(), root.getRootHash().getImprint());
    }

    SparseMerkleTreeRootNode aggregationRoot = aggregationTree.calculateRoot();
    BurnPredicate burnPredicate = BurnPredicate.create(aggregationRoot.getRootHash().getImprint());
    byte[] stateMask = new byte[32];
    RANDOM.nextBytes(stateMask);

    TransferTransaction burnTransaction = TransferTransaction.create(
            token,
            burnPredicate,
            stateMask,
            CborSerializer.encodeNull()
    );

    List<SplitToken> tokens = new ArrayList<>(requestsByTokenId.size());
    for (Entry<TokenId, SplitTokenRequest> entry : requestsByTokenId.entrySet()) {
      SplitTokenRequest request = entry.getValue();
      BigInteger tokenIdPath = entry.getKey().toBitString().toBigInteger();
      Set<Asset> requestAssets = new HashSet<>(request.getAssets());
      List<SplitAssetProof> proofs = requestAssets.stream()
              .map(asset -> SplitAssetProof.create(
                      asset.getId(),
                      aggregationRoot.getPath(asset.getId().toBitString().toBigInteger()),
                      assetTreeRoots.get(asset.getId()).getPath(tokenIdPath)
              ))
              .collect(Collectors.toList());
      tokens.add(new SplitToken(
              networkId,
              request.getRecipient(),
              request.getTokenType(),
              request.getSalt(),
              requestAssets,
              proofs
      ));
    }

    return new SplitResult(burnTransaction, tokens);
  }

}