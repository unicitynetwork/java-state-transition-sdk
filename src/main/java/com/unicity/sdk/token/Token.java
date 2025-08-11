package com.unicity.sdk.token;

import com.unicity.sdk.address.Address;
import com.unicity.sdk.address.ProxyAddress;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProofVerificationStatus;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransactionData;
import com.unicity.sdk.transaction.TransferTransactionData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Token<T extends Transaction<MintTransactionData<?>>> {

  public static final String TOKEN_VERSION = "2.0";

  private final TokenState state;
  private final T genesis;
  private final List<Transaction<TransferTransactionData>> transactions;
  private final Map<Address, Token<?>> nametags;

  public Token(TokenState state, T genesis, List<Transaction<TransferTransactionData>> transactions,
      List<Token<?>> nametagTokens) {
    Objects.requireNonNull(state, "State cannot be null");
    Objects.requireNonNull(genesis, "Genesis cannot be null");
    Objects.requireNonNull(transactions, "Transactions list cannot be null");
    Objects.requireNonNull(nametagTokens, "Nametag tokens list cannot be null");

    this.state = state;
    this.genesis = genesis;
    this.transactions = List.copyOf(transactions);
    Map<Address, Token<?>> nametags = new HashMap<>();
    for (Token<?> token : nametagTokens) {
      if (token == null) {
        throw new IllegalArgumentException("Nametag tokens list cannot contain null elements");
      }

      Address address = ProxyAddress.create(token.getId());
      if (nametags.containsKey(address)) {
        throw new IllegalArgumentException(
            "Nametag tokens list contains duplicate addresses: " + address);
      }
      nametags.put(address, token);
    }
    this.nametags = Map.copyOf(nametags);
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

  public Map<Address, Token<?>> getNametags() {
    return this.nametags;
  }

  public TokenVerificationResult verify() {
    List<TokenVerificationResult> results = new ArrayList<>();
    results.add(
        TokenVerificationResult.fromChildren(
            "Genesis verification",
            List.of(this.verifyGenesis(this.genesis)))
    );

    Transaction<? extends TransactionData<?>> previousTransaction = this.genesis;
    for (Transaction<TransferTransactionData> transaction : this.transactions) {
      Address recipient = previousTransaction.getData().getRecipient();

      results.add(
          TokenVerificationResult.fromChildren(
              "Transaction verification",
              List.of(
                  this.verifyTransaction(transaction, previousTransaction.getData().getDataHash(),
                      recipient)))
      );

      previousTransaction = transaction;
    }

    results.add(TokenVerificationResult.fromChildren(
        "Token data verification",
        List.of(
            this.transactionContainsData(previousTransaction.getData().getDataHash(),
                this.getState().getData())
                ? TokenVerificationResult.success()
                : TokenVerificationResult.fail("Invalid token data")
        )
    ));

    List<TokenVerificationResult> nametagResults = new ArrayList<>();
    for (Token<?> nametag : this.nametags.values()) {
      nametagResults.add(nametag.verify());
    }
    results.add(TokenVerificationResult.fromChildren(
        "Token nametags verification",
        nametagResults
    ));

    Address expectedAddress = this.getState().getUnlockPredicate().getReference(this.getType())
        .toAddress();
    Address recipient = ProxyAddress.resolve(previousTransaction.getData().getRecipient(),
        this.nametags);

    results.add(TokenVerificationResult.fromChildren(
        "Token recipient verification",
        List.of(
            expectedAddress.equals(recipient)
                ? TokenVerificationResult.success()
                : TokenVerificationResult.fail("Invalid recipient address")
        )
    ));

    return TokenVerificationResult.fromChildren("Token verification", results);
  }

  private TokenVerificationResult verifyTransaction(
      Transaction<TransferTransactionData> transaction, DataHash dataHash, Address recipient) {

    for (Token<?> nametag : transaction.getData().getNametags().values()) {
      if (!nametag.verify().isSuccessful()) {
        return TokenVerificationResult.fail(
            String.format("Nametag token %s verification failed", nametag.getId()));
      }
    }

    Address expectedRecipient = transaction.getData().getSourceState().getUnlockPredicate()
        .getReference(this.getType()).toAddress();

    if (!expectedRecipient.equals(
        ProxyAddress.resolve(recipient, transaction.getData().getNametags()))) {
      return TokenVerificationResult.fail("recipient mismatch");
    }

    if (!this.transactionContainsData(dataHash, transaction.getData().getSourceState().getData())) {
      return TokenVerificationResult.fail("data mismatch");
    }

    if (!transaction.getData().getSourceState().getUnlockPredicate()
        .verify(transaction, this.getId(), this.getType())) {
      return TokenVerificationResult.fail("predicate verification failed");
    }

    return TokenVerificationResult.success();
  }

  private TokenVerificationResult verifyGenesis(
      Transaction<MintTransactionData<?>> transaction) {
    if (transaction.getInclusionProof().getAuthenticator() == null) {
      return TokenVerificationResult.fail("Missing authenticator.");
    }
    if (transaction.getInclusionProof().getTransactionHash() == null) {
      return TokenVerificationResult.fail("Missing transaction hash.");
    }

    SigningService signingService = SigningService.createFromSecret(Commitment.MINTER_SECRET,
        transaction.getData().getTokenId().getBytes());

    if (!Arrays.equals(transaction.getInclusionProof().getAuthenticator().getPublicKey(),
        signingService.getPublicKey())) {
      return TokenVerificationResult.fail("Authenticator public key mismatch.");
    }

    if (!transaction.getInclusionProof().getAuthenticator()
        .verify(transaction.getData().calculateHash())) {
      return TokenVerificationResult.fail("Authenticator verification failed.");
    }

    if (transaction.getData().getReason() != null
        && !transaction.getData().getReason().verify(transaction)) {
      return TokenVerificationResult.fail("Mint reason verification failed.");
    }

    RequestId requestId = RequestId.create(signingService.getPublicKey(),
        transaction.getData().getSourceState().getHash());
    if (transaction.getInclusionProof().verify(requestId) != InclusionProofVerificationStatus.OK) {
      return TokenVerificationResult.fail("Inclusion proof verification failed.");
    }

    return TokenVerificationResult.success();
  }

  private boolean transactionContainsData(DataHash hash, byte[] stateData) {
    if ((hash == null) != (stateData == null)) {
      return false;
    }

    if (hash == null) {
      return true;
    }

    DataHasher hasher = new DataHasher(HashAlgorithm.SHA256);
    hasher.update(stateData);
    return hasher.digest().equals(hash);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Token)) {
      return false;
    }
    Token<?> token = (Token<?>) o;
    return Objects.equals(this.state, token.state) && Objects.equals(this.genesis,
        token.genesis) && Objects.equals(this.transactions, token.transactions)
        && Objects.equals(this.nametags, token.nametags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.state, this.genesis, this.transactions, this.nametags);
  }

  @Override
  public String toString() {
    return String.format("Token{state=%s, genesis=%s, transactions=%s, nametagTokens=%s}",
        this.state, this.genesis, this.transactions, this.nametags);
  }
}