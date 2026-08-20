package org.unicitylabs.sdk.payment;

import org.unicitylabs.sdk.api.NetworkId;
import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.DataHasher;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.payment.asset.Asset;
import org.unicitylabs.sdk.payment.asset.AssetId;
import org.unicitylabs.sdk.payment.asset.PaymentAssetCollection;
import org.unicitylabs.sdk.predicate.EncodedPredicate;
import org.unicitylabs.sdk.predicate.builtin.BurnPredicate;
import org.unicitylabs.sdk.smt.LeafExistsException;
import org.unicitylabs.sdk.smt.radixsum.SparseMerkleSumTree;
import org.unicitylabs.sdk.smt.radixsum.SparseMerkleSumTreeRootNode;
import org.unicitylabs.sdk.transaction.StateMask;
import org.unicitylabs.sdk.transaction.Token;
import org.unicitylabs.sdk.transaction.TokenId;
import org.unicitylabs.sdk.transaction.TransferTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Token splitting. Burns the source token and prepares value-conserving output mints, building
 * one radix sparse Merkle sum tree per source asset so that, for each asset, the outputs provably
 * sum to the source amount.
 */
public class TokenSplit {

  private TokenSplit() {
  }

  /** Split using the aggregation service's default timeout and a random burn state mask. */
  public static SplitResult split(
          Token token,
          PaymentDataDeserializer paymentDataDeserializer,
          List<SplitTokenRequest> requests
  ) throws LeafExistsException {
    return split(token, paymentDataDeserializer, requests, 0, StateMask.generate());
  }

  /** Split using the aggregation service's default timeout and the supplied burn state mask. */
  public static SplitResult split(
          Token token,
          PaymentDataDeserializer paymentDataDeserializer,
          List<SplitTokenRequest> requests,
          StateMask burnStateMask
  ) throws LeafExistsException {
    return split(token, paymentDataDeserializer, requests, 0, burnStateMask);
  }

  /**
   * Split a token into new outputs with a random burn state mask.
   *
   * @param token source token to split (the token being burned)
   * @param paymentDataDeserializer decoder for the source token's payment data
   * @param requests per-output mint requests; each carries its own payment data
   * @param burnTimeout exclusive certification request timeout of the burn transaction
   * @return burn predicate, burn transaction and split tokens ready to mint
   * @throws LeafExistsException if duplicate leaves are inserted into a merkle tree
   */
  public static SplitResult split(
          Token token,
          PaymentDataDeserializer paymentDataDeserializer,
          List<SplitTokenRequest> requests,
          long burnTimeout
  ) throws LeafExistsException {
    return TokenSplit.split(token, paymentDataDeserializer, requests, burnTimeout,
            StateMask.generate());
  }

  /**
   * Split a token into new outputs.
   *
   * @param token source token to split (the token being burned)
   * @param paymentDataDeserializer decoder for the source token's payment data
   * @param requests per-output mint requests; each carries its own payment data
   * @param burnTimeout exclusive certification request timeout of the burn transaction
   * @param burnStateMask state mask for the burn transaction; callers needing a crash-resumable
   *     (re-buildable) split supply a deterministically derived mask so the identical burn
   *     transaction can be reconstructed after a failure
   * @return burn predicate, burn transaction and split tokens ready to mint
   * @throws LeafExistsException if duplicate leaves are inserted into a merkle tree
   */
  public static SplitResult split(
          Token token,
          PaymentDataDeserializer paymentDataDeserializer,
          List<SplitTokenRequest> requests,
          long burnTimeout,
          StateMask burnStateMask
  ) throws LeafExistsException {
    Objects.requireNonNull(token, "Token cannot be null");
    Objects.requireNonNull(paymentDataDeserializer, "Payment data deserializer cannot be null");
    Objects.requireNonNull(requests, "Requests cannot be null");
    Objects.requireNonNull(burnStateMask, "Burn state mask cannot be null");

    byte[] paymentDataBytes = token.getGenesis().getData().orElse(null);
    if (paymentDataBytes == null) {
      throw new IllegalArgumentException("Payment data is missing.");
    }

    PaymentAssetCollection sourceAssets =
            paymentDataDeserializer.decode(paymentDataBytes).getAssets();

    NetworkId networkId = token.getGenesis().getNetworkId();
    Map<AssetId, SparseMerkleSumTree> trees = new HashMap<>();
    LinkedHashMap<TokenId, SplitTokenRequest> requestsByTokenId = new LinkedHashMap<>();
    for (SplitTokenRequest request : requests) {
      Objects.requireNonNull(request, "Split token request cannot be null");
      TokenId tokenId = TokenId.fromSalt(networkId, request.getSalt());
      if (requestsByTokenId.containsKey(tokenId)) {
        throw new DuplicateSplitTokenIdException(tokenId.toString());
      }
      requestsByTokenId.put(tokenId, request);

      EncodedPredicate recipient = EncodedPredicate.fromPredicate(request.getRecipient());
      byte[] data = request.getPaymentData().encode();
      byte[] leafData = SplitMintJustification.calculateLeafData(
              token, recipient, request.getSalt(), tokenId, data);

      for (Asset asset : request.getPaymentData().getAssets().toList()) {
        if (sourceAssets.get(asset.getId()) == null) {
          throw new TokenAssetMissingException(asset.getId());
        }

        SparseMerkleSumTree tree = trees.computeIfAbsent(asset.getId(),
                id -> new SparseMerkleSumTree(HashAlgorithm.SHA256));
        tree.addLeaf(tokenId.getBytes(), leafData, asset.getValue());
      }
    }

    if (trees.size() != sourceAssets.size()) {
      throw new TokenAssetCountMismatchException();
    }

    List<DataHash> roots = new ArrayList<>();
    Map<AssetId, SparseMerkleSumTreeRootNode> rootByAsset = new HashMap<>();
    for (Asset asset : sourceAssets.toList()) {
      SparseMerkleSumTree tree = trees.get(asset.getId());
      if (tree == null) {
        throw new TokenAssetMissingException(asset.getId());
      }

      SparseMerkleSumTreeRootNode root = tree.calculateRoot();
      if (!root.getValue().equals(asset.getValue())) {
        throw new TokenAssetValueMismatchException(asset.getId(), asset.getValue(),
                root.getValue());
      }

      roots.add(root.getHash());
      rootByAsset.put(asset.getId(), root);
    }

    byte[] manifestBytes = SplitManifest.create(roots).toCbor();
    byte[] burnReason = new DataHasher(HashAlgorithm.SHA256).update(manifestBytes).digest()
            .getData();
    BurnPredicate burnPredicate = BurnPredicate.create(burnReason);
    TransferTransaction burnTransaction = TransferTransaction.create(
            token,
            burnPredicate,
            burnStateMask,
            burnTimeout,
            manifestBytes
    );

    List<SplitToken> tokens = new ArrayList<>(requestsByTokenId.size());
    for (Map.Entry<TokenId, SplitTokenRequest> entry : requestsByTokenId.entrySet()) {
      SplitTokenRequest request = entry.getValue();
      TokenId tokenId = entry.getKey();
      List<SplitAllocationProof> proofs = request.getPaymentData().getAssets().toList().stream()
              .map(asset -> SplitAllocationProof.create(
                      rootByAsset.get(asset.getId()), tokenId.getBytes()))
              .collect(Collectors.toList());

      tokens.add(new SplitToken(
              networkId,
              request.getRecipient(),
              token.getType(),
              request.getSalt(),
              request.getPaymentData(),
              proofs
      ));
    }

    return new SplitResult(burnPredicate, burnTransaction, tokens);
  }
}
