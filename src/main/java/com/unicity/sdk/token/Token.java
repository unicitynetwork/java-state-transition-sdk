package com.unicity.sdk.token;

import com.unicity.sdk.address.Address;
import com.unicity.sdk.address.ProxyAddress;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.InclusionProofVerificationStatus;
import com.unicity.sdk.transaction.MintCommitment;
import com.unicity.sdk.transaction.MintTransactionState;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransactionData;
import com.unicity.sdk.transaction.TransferTransactionData;
import com.unicity.sdk.util.VerificationResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Token<T extends Transaction<MintTransactionData<?>>> {

  public static final String TOKEN_VERSION = "2.0";

  private final TokenState state;
  private final T genesis;
  private final List<Transaction<TransferTransactionData>> transactions;
  private final List<Token<?>> nametags;

  public Token(TokenState state, T genesis, List<Transaction<TransferTransactionData>> transactions,
      List<Token<?>> nametags) {
    Objects.requireNonNull(state, "State cannot be null");
    Objects.requireNonNull(genesis, "Genesis cannot be null");
    Objects.requireNonNull(transactions, "Transactions list cannot be null");
    Objects.requireNonNull(nametags, "Nametag tokens list cannot be null");

    this.state = state;
    this.genesis = genesis;
    this.transactions = List.copyOf(transactions);
    this.nametags = List.copyOf(nametags);
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

  public Optional<byte[]> getData() {
    return this.genesis.getData().getTokenData();
  }

  public Optional<TokenCoinData> getCoins() {
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

  public List<Token<?>> getNametags() {
    return this.nametags;
  }

  public Token<T> update(
      TokenState state,
      Transaction<TransferTransactionData> transaction,
      List<Token<?>> transactionNametags
  ) {
    Objects.requireNonNull(state, "State is null");
    Objects.requireNonNull(transaction, "Transaction is null");
    Objects.requireNonNull(transactionNametags, "Nametag tokens are null");

    if (!transaction.getData().getSourceState().getUnlockPredicate()
        .verify(transaction, this.getId(), this.getType())) {
      throw new RuntimeException("Predicate verification failed");
    }

    Address recipient = ProxyAddress.resolve(
        transaction.getData().getRecipient(),
        transactionNametags
    );
    if (!state.getUnlockPredicate().getReference(this.getType()).toAddress().equals(recipient)) {
      throw new RuntimeException("Recipient address mismatch");
    }

    if (!transaction.containsData(state.getData().orElse(null))) {
      throw new RuntimeException("State data is not part of transaction.");
    }

    List<Transaction<TransferTransactionData>> transactions = new ArrayList<>(
        this.getTransactions());
    transactions.add(transaction);

    return new Token<>(state, this.getGenesis(), transactions, transactionNametags);
  }

  public VerificationResult verify() {
    List<VerificationResult> results = new ArrayList<>();
    results.add(
        VerificationResult.fromChildren(
            "Genesis verification",
            List.of(this.verifyGenesis(this.genesis)))
    );

    Transaction<? extends TransactionData<?>> previousTransaction = this.genesis;
    for (Transaction<TransferTransactionData> transaction : this.transactions) {
      Address recipient = previousTransaction.getData().getRecipient();

      results.add(
          VerificationResult.fromChildren(
              "Transaction verification",
              List.of(
                  this.verifyTransaction(
                      transaction,
                      previousTransaction.getData().getDataHash().orElse(null),
                      recipient
                  )
              )
          )
      );

      previousTransaction = transaction;
    }

    results.add(VerificationResult.fromChildren(
        "Token data verification",
        List.of(
            this.transactionContainsData(
                previousTransaction.getData().getDataHash().orElse(null),
                this.getState().getData().orElse(null)
            )
                ? VerificationResult.success()
                : VerificationResult.fail("Invalid token data")
        )
    ));

    List<VerificationResult> nametagResults = new ArrayList<>();
    for (Token<?> nametag : this.nametags) {
      nametagResults.add(nametag.verify());
    }
    results.add(VerificationResult.fromChildren(
        "Token nametags verification",
        nametagResults
    ));

    Address expectedAddress = this.getState().getUnlockPredicate().getReference(this.getType())
        .toAddress();

    Address recipient = ProxyAddress.resolve(previousTransaction.getData().getRecipient(),
        this.nametags);

    results.add(VerificationResult.fromChildren(
        "Token recipient verification",
        List.of(
            expectedAddress.equals(recipient)
                ? VerificationResult.success()
                : VerificationResult.fail("Invalid recipient address")
        )
    ));

    return VerificationResult.fromChildren("Token verification", results);
  }

  private VerificationResult verifyTransaction(
      Transaction<TransferTransactionData> transaction, DataHash dataHash, Address recipient) {

    for (Token<?> nametag : transaction.getData().getNametags()) {
      if (!nametag.verify().isSuccessful()) {
        return VerificationResult.fail(
            String.format("Nametag token %s verification failed", nametag.getId()));
      }
    }

    Address expectedRecipient = transaction.getData().getSourceState().getUnlockPredicate()
        .getReference(this.getType()).toAddress();

    if (!expectedRecipient.equals(
        ProxyAddress.resolve(recipient, transaction.getData().getNametags()))) {
      return VerificationResult.fail("recipient mismatch");
    }

    if (!this.transactionContainsData(
        dataHash,
        transaction.getData().getSourceState().getData().orElse(null))) {
      return VerificationResult.fail("data mismatch");
    }

    if (!transaction.getData().getSourceState().getUnlockPredicate()
        .verify(transaction, this.getId(), this.getType())) {
      return VerificationResult.fail("predicate verification failed");
    }

    return VerificationResult.success();
  }

  private VerificationResult verifyGenesis(
      Transaction<MintTransactionData<?>> transaction) {
    if (!transaction.getInclusionProof().getAuthenticator().isPresent()) {
      return VerificationResult.fail("Missing authenticator.");
    }

    if (!transaction.getInclusionProof().getTransactionHash().isPresent()) {
      return VerificationResult.fail("Missing transaction hash.");
    }

    if (!transaction.getData().getSourceState()
        .equals(MintTransactionState.create(transaction.getData().getTokenId()))) {
      return VerificationResult.fail("Invalid source state");
    }

    SigningService signingService = MintCommitment.createSigningService(transaction.getData());

    if (!Arrays.equals(transaction.getInclusionProof().getAuthenticator().get().getPublicKey(),
        signingService.getPublicKey())) {
      return VerificationResult.fail("Authenticator public key mismatch.");
    }

    if (!transaction.getInclusionProof().getAuthenticator().get()
        .verify(transaction.getData().calculateHash())) {
      return VerificationResult.fail("Authenticator verification failed.");
    }

    VerificationResult reasonResult = VerificationResult.fromChildren(
        "Mint reason verification",
        List.of(
            transaction.getData().getReason()
                .map(reason -> reason.verify(transaction))
                .orElse(VerificationResult.success())
        )
    );
    if (!reasonResult.isSuccessful()) {
      return reasonResult;
    }

    RequestId requestId = RequestId.create(signingService.getPublicKey(),
        transaction.getData().getSourceState().getHash());
    if (transaction.getInclusionProof().verify(requestId) != InclusionProofVerificationStatus.OK) {
      return VerificationResult.fail("Inclusion proof verification failed.");
    }

    return VerificationResult.success();
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