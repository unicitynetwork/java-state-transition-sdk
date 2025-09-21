package org.unicitylabs.sdk.token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.address.ProxyAddress;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.PredicateEngineService;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.InclusionProofVerificationStatus;
import org.unicitylabs.sdk.transaction.MintCommitment;
import org.unicitylabs.sdk.transaction.MintTransactionData;
import org.unicitylabs.sdk.transaction.MintTransactionState;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferTransactionData;
import org.unicitylabs.sdk.verification.VerificationResult;

public class Token<T extends MintTransactionData<?>> {

  public static final String TOKEN_VERSION = "2.0";

  private final TokenState state;
  private final Transaction<T> genesis;
  private final List<Transaction<TransferTransactionData>> transactions;
  private final List<Token<?>> nametags;

  public Token(
      TokenState state,
      Transaction<T> genesis,
      List<Transaction<TransferTransactionData>> transactions,
      List<Token<?>> nametags
  ) {
    Objects.requireNonNull(state, "State cannot be null");
    Objects.requireNonNull(genesis, "Genesis cannot be null");
    Objects.requireNonNull(transactions, "Transactions list cannot be null");
    Objects.requireNonNull(nametags, "Nametag tokens list cannot be null");

    this.state = state;
    this.genesis = genesis;
    this.transactions = List.copyOf(transactions);
    this.nametags = List.copyOf(nametags);
  }

  public Token(TokenState state, Transaction<T> genesis, List<Token<?>> nametags) {
    this(state, genesis, List.of(), nametags);
  }

  public Token(TokenState state, Transaction<T> genesis) {
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

  public Transaction<T> getGenesis() {
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

    if (!this.verifyTransaction(this, transaction).isSuccessful()) {
      // TODO: Add method to return why it failed
      throw new RuntimeException("Transaction verification failed");
    }

    LinkedList<Transaction<TransferTransactionData>> transactions = new LinkedList<>(
        this.transactions);
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

    for (int i = 0; i < this.transactions.size(); i++) {
      Transaction<TransferTransactionData> transaction = this.transactions.get(i);

      results.add(
          VerificationResult.fromChildren(
              "Transaction verification",
              List.of(
                  this.verifyTransaction(
                      new Token<>(
                          transaction.getData().getSourceState(),
                          this.genesis,
                          this.transactions.subList(0, i),
                          transaction.getData().getNametags()
                      ),
                      transaction
                  )
              )
          )
      );
    }

    results.add(VerificationResult.fromChildren(
        "Token current state verification",
        List.of(this.verifyTransaction(this, null))
    ));

    return VerificationResult.fromChildren("Token verification", results);
  }

  private VerificationResult verifyTransaction(
      Token<?> token,
      Transaction<TransferTransactionData> transaction
  ) {
    for (Token<?> nametag : token.getNametags()) {
      if (!nametag.verify().isSuccessful()) {
        return VerificationResult.fail(
            String.format("Nametag token %s verification failed", nametag.getId()));
      }
    }

    Predicate predicate = PredicateEngineService.createPredicate(token.getState().getPredicate());
    Address expectedRecipient = predicate.getReference().toAddress();

    Transaction<?> previousTransaction = !token.transactions.isEmpty()
        ? token.transactions.getLast()
        : token.genesis;
    if (!expectedRecipient.equals(
        ProxyAddress.resolve(previousTransaction.getData().getRecipient(), token.getNametags()))) {
      return VerificationResult.fail("recipient mismatch");
    }

    if (!this.transactionContainsData(
        previousTransaction.getData().getDataHash().orElse(null),
        token.getState().getData().orElse(null))) {
      return VerificationResult.fail("data mismatch");
    }

    if (transaction != null && !predicate.verify(token, transaction)) {
      return VerificationResult.fail("predicate verification failed");
    }

    return VerificationResult.success();
  }

  private VerificationResult verifyGenesis(Transaction<T> transaction) {
    if (transaction.getInclusionProof().getAuthenticator().isEmpty()) {
      return VerificationResult.fail("Missing authenticator.");
    }

    if (transaction.getInclusionProof().getTransactionHash().isEmpty()) {
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
    return String.format("Token{state=%s, genesis=%s, transactions=%s, nametags=%s}",
        this.state, this.genesis, this.transactions, this.nametags);
  }
}