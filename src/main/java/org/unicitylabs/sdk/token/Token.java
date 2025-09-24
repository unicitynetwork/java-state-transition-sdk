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
import org.unicitylabs.sdk.bft.RootTrustBase;
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
import org.unicitylabs.sdk.verification.VerificationException;
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

  public static <T extends MintTransactionData<?>> Token<T> create(
      RootTrustBase trustBase,
      TokenState state,
      Transaction<T> transaction
  ) throws VerificationException {
    return Token.create(trustBase, state, transaction, List.of());
  }

  public static <T extends MintTransactionData<?>> Token<T> create(
      RootTrustBase trustBase,
      TokenState state,
      Transaction<T> transaction,
      List<Token<?>> nametags
  ) throws VerificationException {
    Objects.requireNonNull(state, "State cannot be null");
    Objects.requireNonNull(transaction, "Genesis cannot be null");
    Objects.requireNonNull(trustBase, "Trust base cannot be null");
    Objects.requireNonNull(nametags, "Nametag tokens cannot be null");

    Token<T> token = new Token<>(state, transaction, List.of(), nametags);
    VerificationResult result = token.verify(trustBase);
    if (!result.isSuccessful()) {
      throw new VerificationException("Token verification failed", result);
    }

    return token;
  }

  public Token<T> update(
      RootTrustBase trustBase,
      TokenState state,
      Transaction<TransferTransactionData> transaction,
      List<Token<?>> nametags
  ) throws VerificationException {
    Objects.requireNonNull(state, "State cannot be null");
    Objects.requireNonNull(transaction, "Transaction cannot be null");
    Objects.requireNonNull(nametags, "Nametag tokens cannot be null");
    Objects.requireNonNull(trustBase, "Trust base cannot be null");

    VerificationResult result = Token.verifyTransaction(this, transaction, trustBase);

    if (!result.isSuccessful()) {
      throw new VerificationException("Transaction verification failed", result);
    }

    LinkedList<Transaction<TransferTransactionData>> transactions = new LinkedList<>(
        this.transactions
    );
    transactions.add(transaction);

    return new Token<>(state, this.getGenesis(), transactions, nametags);
  }

  public VerificationResult verify(RootTrustBase trustBase) {
    List<VerificationResult> results = new ArrayList<>();
    results.add(
        VerificationResult.fromChildren(
            "Genesis verification",
            List.of(Token.verifyGenesis(this.genesis, trustBase)))
    );

    for (int i = 0; i < this.transactions.size(); i++) {
      Transaction<TransferTransactionData> transaction = this.transactions.get(i);

      results.add(
          VerificationResult.fromChildren(
              "Transaction verification",
              List.of(
                  Token.verifyTransaction(
                      new Token<>(
                          transaction.getData().getSourceState(),
                          this.genesis,
                          this.transactions.subList(0, i),
                          transaction.getData().getNametags()
                      ),
                      transaction,
                      trustBase
                  )
              )
          )
      );
    }

    results.add(VerificationResult.fromChildren(
        "Token current state verification",
        List.of(Token.verifyTransaction(this, null, trustBase))
    ));

    return VerificationResult.fromChildren("Token verification", results);
  }

  private static VerificationResult verifyTransaction(
      Token<?> token,
      Transaction<TransferTransactionData> transaction,
      RootTrustBase trustBase
  ) {
    for (Token<?> nametag : token.getNametags()) {
      if (!nametag.verify(trustBase).isSuccessful()) {
        return VerificationResult.fail(
            String.format("Nametag token %s verification failed", nametag.getId()));
      }
    }

    Predicate predicate = PredicateEngineService.createPredicate(token.getState().getPredicate());
    Address expectedRecipient = predicate.getReference().toAddress();

    Transaction<?> previousTransaction = !token.transactions.isEmpty()
        ? token.transactions.get(token.transactions.size() - 1)
        : token.genesis;
    if (!expectedRecipient.equals(
        ProxyAddress.resolve(previousTransaction.getData().getRecipient(), token.getNametags()))) {
      return VerificationResult.fail("recipient mismatch");
    }

    if (!Token.transactionContainsData(
        previousTransaction.getData().getDataHash().orElse(null),
        token.getState().getData().orElse(null))) {
      return VerificationResult.fail("data mismatch");
    }

    if (transaction != null && !predicate.verify(token, transaction, trustBase)) {
      return VerificationResult.fail("predicate verification failed");
    }

    return VerificationResult.success();
  }

  private static <T extends MintTransactionData<?>> VerificationResult verifyGenesis(
      Transaction<T> transaction,
      RootTrustBase trustBase
  ) {
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
    if (transaction.getInclusionProof().verify(requestId, trustBase)
        != InclusionProofVerificationStatus.OK) {
      return VerificationResult.fail("Inclusion proof verification failed.");
    }

    return VerificationResult.success();
  }

  private static boolean transactionContainsData(DataHash hash, byte[] stateData) {
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