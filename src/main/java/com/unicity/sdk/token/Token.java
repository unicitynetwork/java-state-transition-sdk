package com.unicity.sdk.token;

import com.unicity.sdk.address.Address;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.fungible.SplitMintReason;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProofVerificationStatus;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransactionData;
import com.unicity.sdk.transaction.TransferTransactionData;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Token<T extends Transaction<MintTransactionData<?>>> {

  public static final String TOKEN_VERSION = "2.0";

  private final TokenState state;
  private final T genesis;
  private final List<Transaction<TransferTransactionData>> transactions;
  private final List<Token<?>> nametagTokens;

  public Token(TokenState state, T genesis, List<Transaction<TransferTransactionData>> transactions,
      List<Token<?>> nametagTokens) {
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

  public List<Token<?>> getNametagTokens() {
    return this.nametagTokens;
  }

  public boolean verify() throws IOException {
    this.genesis.verify()
    if (!this.verifyGenesis(this.genesis)) {
      return false;
    }

    Transaction<? extends TransactionData<?>> previousTransaction = this.genesis;
    for (Transaction<TransferTransactionData> transaction : this.transactions) {
      Address expectedRecipient = transaction.getData().getSourceState().getUnlockPredicate()
          .getReference(this.getType()).toAddress();
      if (!expectedRecipient.equals(previousTransaction.getData().getRecipient())) {
        return false;
      }

      if (!previousTransaction.containsData(transaction.getData().getSourceState().getData())) {
        return false;
      }

      if (!transaction.getData().getSourceState().getUnlockPredicate()
          .verify(transaction, this.getId(), this.getType())) {
        return false;
      }

      previousTransaction = transaction;
    }

    if (previousTransaction.containsData(this.getState().getData())) {
      return false;
    }

    Address expectedAddress = this.getState().getUnlockPredicate().getReference(this.getType())
        .toAddress();
    if (!expectedAddress.equals(previousTransaction.getData().getRecipient())) {
      return false;
    }

    return true;
  }

  private boolean verifyGenesis(
      Transaction<MintTransactionData<?>> transaction) throws IOException {
    if (transaction.getInclusionProof().getAuthenticator() == null ||
        transaction.getInclusionProof().getTransactionHash() == null) {
      return false; // Missing authenticator or transaction hash
    }

    SigningService signingService = SigningService.createFromSecret(Commitment.MINTER_SECRET,
        transaction.getData().getTokenId().getBytes());

    if (!Arrays.equals(transaction.getInclusionProof().getAuthenticator().getPublicKey(),
        signingService.getPublicKey())) {
      return false;
    }

    if (!transaction.getInclusionProof().getAuthenticator().verify(transaction.getData().calculateHash())) {
      return false; // Authenticator verification failed
    }

    if (transaction.getData().getReason() instanceof SplitMintReason) {
      // transaction.getData().getReason().verify(transaction);
      return true;
    }

    RequestId requestId = RequestId.create(signingService.getPublicKey(), transaction.getData().getSourceState().getHash());
    return transaction.getInclusionProof().verify(requestId) == InclusionProofVerificationStatus.OK;
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