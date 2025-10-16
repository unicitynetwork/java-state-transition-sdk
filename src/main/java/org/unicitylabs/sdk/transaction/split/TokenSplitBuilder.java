package org.unicitylabs.sdk.transaction.split;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.BranchExistsException;
import org.unicitylabs.sdk.mtree.LeafOutOfBoundsException;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTree;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreeRootNode;
import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTree;
import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTree.LeafValue;
import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTreeRootNode;
import org.unicitylabs.sdk.predicate.embedded.BurnPredicate;
import org.unicitylabs.sdk.predicate.embedded.BurnPredicateReference;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.MintCommitment;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.transaction.TransferCommitment;
import org.unicitylabs.sdk.transaction.TransferTransaction;
import org.unicitylabs.sdk.verification.VerificationException;

/**
 * Token splitting builder.
 */
public class TokenSplitBuilder {

  private final Map<TokenId, TokenRequest> tokens = new HashMap<>();

  /**
   * Create new token which will be created from selected token.
   *
   * @param id                new token id
   * @param type              new token type
   * @param data              new token data
   * @param coinData          new token coin data
   * @param recipient         new token recipient address
   * @param salt              new token salt
   * @param recipientDataHash new token recipient data hash
   * @return current builder
   */
  public TokenSplitBuilder createToken(
      TokenId id,
      TokenType type,
      byte[] data,
      TokenCoinData coinData,
      Address recipient,
      byte[] salt,
      DataHash recipientDataHash
  ) {
    this.tokens.put(id,
        new TokenRequest(id, type, data, coinData, recipient, salt, recipientDataHash));

    return this;
  }

  /**
   * Split old token to new tokens.
   *
   * @param token token to be used for split
   * @return token split object for submitting info
   * @throws LeafOutOfBoundsException if building aggregation tree and coin tree fail
   * @throws BranchExistsException    if building aggregation tree and coin tree fail
   */
  public TokenSplit build(Token<?> token) throws LeafOutOfBoundsException, BranchExistsException {
    Objects.requireNonNull(token, "Token cannot be null");

    Map<CoinId, SparseMerkleSumTree> trees = new HashMap<>();

    for (TokenRequest data : this.tokens.values()) {
      for (Map.Entry<CoinId, BigInteger> coin : data.coinData.getCoins().entrySet()) {
        SparseMerkleSumTree tree = trees.computeIfAbsent(coin.getKey(),
            k -> new SparseMerkleSumTree(HashAlgorithm.SHA256));
        tree.addLeaf(data.id.toBitString().toBigInteger(),
            new LeafValue(coin.getKey().getBytes(), coin.getValue()));
      }
    }

    Map<CoinId, BigInteger> tokenCoins = token.getCoins().map(TokenCoinData::getCoins)
        .orElse(Map.of());
    if (trees.size() != tokenCoins.size()) {
      throw new IllegalArgumentException("Token has different number of coins than expected");
    }

    SparseMerkleTree aggregationTree = new SparseMerkleTree(HashAlgorithm.SHA256);
    Map<CoinId, SparseMerkleSumTreeRootNode> coinRoots = new HashMap<>();
    for (Entry<CoinId, SparseMerkleSumTree> tree : trees.entrySet()) {
      BigInteger coinsInToken = Optional.ofNullable(tokenCoins.get(tree.getKey()))
          .orElse(BigInteger.ZERO);
      SparseMerkleSumTreeRootNode root = tree.getValue().calculateRoot();
      if (root.getRoot().getCounter().compareTo(coinsInToken) != 0) {
        throw new IllegalArgumentException(
            String.format("Token contained %s %s coins, but tree has %s",
                coinsInToken, tree.getKey(), root.getRoot().getCounter()));
      }

      coinRoots.put(tree.getKey(), root);
      aggregationTree.addLeaf(tree.getKey().toBitString().toBigInteger(),
          root.getRoot().getHash().getImprint());
    }

    return new TokenSplit(
        token,
        aggregationTree.calculateRoot(),
        coinRoots,
        this.tokens
    );
  }

  /**
   * Token split request object.
   */
  public static class TokenSplit {

    private final Token<?> token;
    private final SparseMerkleTreeRootNode aggregationRoot;
    private final Map<CoinId, SparseMerkleSumTreeRootNode> coinRoots;
    private final Map<TokenId, TokenRequest> tokens;

    private TokenSplit(
        Token<?> token,
        SparseMerkleTreeRootNode aggregationRoot,
        Map<CoinId, SparseMerkleSumTreeRootNode> coinRoots,
        Map<TokenId, TokenRequest> tokens
    ) {
      this.token = token;
      this.aggregationRoot = aggregationRoot;
      this.coinRoots = coinRoots;
      this.tokens = tokens;
    }

    /**
     * Create burn commitment to burn token going through split.
     *
     * @param salt           burn commitment salt
     * @param signingService signing service used to unlock token
     * @return transfer commitment for sending to unicity service
     */
    public TransferCommitment createBurnCommitment(byte[] salt, SigningService signingService) {
      return TransferCommitment.create(
          token,
          BurnPredicateReference.create(
              this.token.getType(),
              this.aggregationRoot.getRootHash()
          ).toAddress(),
          salt,
          null,
          null,
          signingService
      );
    }

    /**
     * Create split mint commitments after burn transaction is received.
     *
     * @param trustBase       trust base for burn transaction verification
     * @param burnTransaction burn transaction
     * @return list of mint commitments for sending to unicity service
     * @throws VerificationException if token verification fails
     */
    public List<MintCommitment<SplitMintReason>> createSplitMintCommitments(
        RootTrustBase trustBase,
        TransferTransaction burnTransaction
    ) throws VerificationException {
      Objects.requireNonNull(burnTransaction, "Burn transaction cannot be null");

      Token<?> burnedToken = this.token.update(
          trustBase,
          new TokenState(
              new BurnPredicate(
                  this.token.getId(),
                  this.token.getType(),
                  this.aggregationRoot.getRootHash()
              ),
              null
          ),
          burnTransaction,
          List.of()
      );

      return List.copyOf(
          this.tokens.values().stream()
              .map(request -> MintCommitment.create(
                      new MintTransaction.Data<>(
                          request.id,
                          request.type,
                          request.data,
                          request.coinData,
                          request.recipient,
                          request.salt,
                          request.recipientDataHash,
                          new SplitMintReason(
                              burnedToken,
                              List.copyOf(
                                  request.coinData.getCoins().keySet().stream()
                                      .map(coinId -> new SplitMintReasonProof(
                                              coinId,
                                              this.aggregationRoot
                                                  .getPath(coinId.toBitString().toBigInteger()),
                                              this.coinRoots.get(coinId)
                                                  .getPath(request.id.toBitString().toBigInteger())
                                          )
                                      )
                                      .collect(Collectors.toList())
                              )
                          )
                      )
                  )
              )
              .collect(Collectors.toList())
      );
    }
  }

  /**
   * New token request for generating it out of burnt token.
   */
  public static class TokenRequest {

    private final TokenId id;
    private final TokenType type;
    private final byte[] data;
    private final TokenCoinData coinData;
    private final Address recipient;
    private final byte[] salt;
    private final DataHash recipientDataHash;

    TokenRequest(
        TokenId id,
        TokenType type,
        byte[] data,
        TokenCoinData coinData,
        Address recipient,
        byte[] salt,
        DataHash recipientDataHash
    ) {
      Objects.requireNonNull(id, "Token cannot be null");
      Objects.requireNonNull(type, "Token type cannot be null");
      Objects.requireNonNull(recipient, "Recipient cannot be null");
      Objects.requireNonNull(salt, "Salt cannot be null");
      if (coinData == null || coinData.getCoins().isEmpty()) {
        throw new IllegalArgumentException("Token must have at least one coin");
      }

      this.id = id;
      this.type = type;
      this.data = data == null ? null : Arrays.copyOf(data, data.length);
      this.coinData = coinData;
      this.recipient = recipient;
      this.salt = Arrays.copyOf(salt, salt.length);
      this.recipientDataHash = recipientDataHash;
    }
  }
}
