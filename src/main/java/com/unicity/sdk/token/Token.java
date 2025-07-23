package com.unicity.sdk.token;

import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferTransactionData;
import java.util.List;
import java.util.Objects;

public class Token<T extends Transaction<MintTransactionData<?>>> {

  public static final String TOKEN_VERSION = "2.0";

  private final TokenState state;
  private final T genesis;
  private final List<Transaction<TransferTransactionData>> transactions;
  private final List<NameTagToken> nametagTokens;

  public Token(TokenState state, T genesis, List<Transaction<TransferTransactionData>> transactions,
      List<NameTagToken> nametagTokens) {
    Objects.requireNonNull(state, "State cannot be null");
    Objects.requireNonNull(genesis, "Genesis cannot be null");
    Objects.requireNonNull(transactions, "Transactions list cannot be null");
    Objects.requireNonNull(nametagTokens, "Nametag tokens list cannot be null");

    this.state = state;
    this.genesis = genesis;
    this.transactions = List.copyOf(transactions);
    this.nametagTokens = List.copyOf(nametagTokens);
  }

  public Token(TokenState state, T genesis) {
    this(state, genesis, List.of(), List.of());
  }

  public TokenId getId() {
    return this.genesis.getData().getTokenId();
  }

  public TokenType getType() {
    return this.genesis.getData().getTokenType();
  }

  public byte[] getData() {
    return this.genesis.getData().getTokenData();
  }

  public TokenCoinData getCoins() {
    return this.genesis.getData().getCoinData();
  }

  public String getVersion() {
    return TOKEN_VERSION;
  }

  public TokenState getState() {
    return this.state;
  }

  public T getGenesis() {
    return this.genesis;
  }

  public List<Transaction<TransferTransactionData>> getTransactions() {
    return this.transactions;
  }

  public List<NameTagToken> getNametagTokens() {
    return this.nametagTokens;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Token)) {
      return false;
    }
    Token<?> token = (Token<?>) o;
    return Objects.equals(this.state, token.state) && Objects.equals(this.genesis,
        token.genesis) && Objects.equals(this.transactions, token.transactions)
        && Objects.equals(this.nametagTokens, token.nametagTokens);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.state, this.genesis, this.transactions, this.nametagTokens);
  }

  @Override
  public String toString() {
    return String.format("Token{state=%s, genesis=%s, transactions=%s, nametagTokens=%s}",
        this.state, this.genesis, this.transactions, this.nametagTokens);
  }
}