package com.unicity.sdk.transaction.split;

import com.unicity.sdk.address.Address;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.mtree.BranchExistsException;
import com.unicity.sdk.mtree.LeafOutOfBoundsException;
import com.unicity.sdk.mtree.plain.SparseMerkleTree;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTree;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTree.LeafValue;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTreeRootNode;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.CoinId;
import com.unicity.sdk.token.fungible.TokenCoinData;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

public class TokenSplitBuilder {

  private final Map<TokenId, TokenRequest> tokens = new HashMap<>();

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

  public void build(Token<?> token) throws LeafOutOfBoundsException, BranchExistsException {
    Map<CoinId, SparseMerkleSumTree> trees = new HashMap<>();
    Map<CoinId, List<TokenRequest>> tokensByCoin = new HashMap<>();

    for (TokenRequest data : this.tokens.values()) {
      for (Map.Entry<CoinId, BigInteger> coin : data.coinData.getCoins().entrySet()) {
        SparseMerkleSumTree tree = trees.computeIfAbsent(coin.getKey(),
            k -> new SparseMerkleSumTree(HashAlgorithm.SHA256));
        tree.addLeaf(data.id.toBitString().toBigInteger(),
            new LeafValue(coin.getKey().getBytes(), coin.getValue()));
        tokensByCoin.computeIfAbsent(coin.getKey(), k -> new java.util.ArrayList<>())
            .add(data);
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

    // BURN
    // Create tokens
  }

  public static class TokenRequest {
    private final TokenId id;
    private final TokenType type;
    private final byte[] data;
    private final TokenCoinData coinData;
    private final Address recipient;
    private final byte[] salt;
    private final DataHash recipientDataHash;

    public TokenRequest(
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
      this.data = data;
      this.coinData = coinData;
      this.recipient = recipient;
      this.salt = salt;
      this.recipientDataHash = recipientDataHash;
    }
  }
}
