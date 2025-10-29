package org.unicitylabs.sdk.token;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.address.ProxyAddress;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.PredicateEngineService;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.InclusionProofVerificationStatus;
import org.unicitylabs.sdk.transaction.MintCommitment;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.transaction.MintTransactionReason;
import org.unicitylabs.sdk.transaction.MintTransactionState;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferTransaction;
import org.unicitylabs.sdk.verification.VerificationException;
import org.unicitylabs.sdk.verification.VerificationResult;

/**
 * Token representation.
 *
 * @param <R> mint transaction reason for current token.
 */
public class Token<R extends MintTransactionReason> {

  /**
   * Current token representation version.
   */
  public static final String TOKEN_VERSION = "2.0";

  private final TokenState state;
  private final MintTransaction<R> genesis;
  private final List<TransferTransaction> transactions;
  private final List<Token<?>> nametags;

  @JsonCreator
  Token(
      @JsonProperty("state")
      TokenState state,
      @JsonProperty("genesis")
      MintTransaction<R> genesis,
      @JsonProperty("transactions")
      List<TransferTransaction> transactions,
      @JsonProperty("nametags")
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

  /**
   * Get token id from genesis.
   *
   * @return token id
   */
  @JsonIgnore
  public TokenId getId() {
    return this.genesis.getData().getTokenId();
  }

  /**
   * Get token type from genesis.
   *
   * @return token type
   */
  @JsonIgnore
  public TokenType getType() {
    return this.genesis.getData().getTokenType();
  }

  /**
   * Get token immutable data from genesis.
   *
   * @return token immutable data
   */
  @JsonIgnore
  public Optional<byte[]> getData() {
    return this.genesis.getData().getTokenData();
  }

  /**
   * Get token coins data from genesis.
   *
   * @return token coins data
   */
  @JsonIgnore
  public Optional<TokenCoinData> getCoins() {
    return this.genesis.getData().getCoinData();
  }

  /**
   * Get token version.
   *
   * @return token version
   */
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public String getVersion() {
    return TOKEN_VERSION;
  }

  /**
   * Get token current state.
   *
   * @return token state
   */
  public TokenState getState() {
    return this.state;
  }

  /**
   * Get token genesis.
   *
   * @return token genesis
   */
  public MintTransaction<R> getGenesis() {
    return this.genesis;
  }

  /**
   * Get token transactions.
   *
   * @return token transactions
   */
  public List<TransferTransaction> getTransactions() {
    return this.transactions;
  }

  /**
   * Get token current state nametags.
   *
   * @return nametags
   */
  public List<Token<?>> getNametags() {
    return this.nametags;
  }

  /**
   * Create token from mint transaction and initial state. Also verify if state is correct.
   *
   * @param trustBase   trust base for mint transaction verification
   * @param state       initial state
   * @param transaction mint transaction
   * @param <R>         mint transaction reason
   * @return token
   * @throws VerificationException if token state is invalid
   */
  public static <R extends MintTransactionReason> Token<R> create(
      RootTrustBase trustBase,
      TokenState state,
      MintTransaction<R> transaction
  ) throws VerificationException {
    return Token.create(trustBase, state, transaction, List.of());
  }

  /**
   * Create token state from mint transaction, initial state and nametags. Also verify if state is
   * correct.
   *
   * @param trustBase   trust base for mint transaction verification
   * @param state       initial state
   * @param transaction mint transaction
   * @param nametags    nametags associated with transaction
   * @param <R>         mint transaction reason
   * @return token
   * @throws VerificationException if token state is invalid
   */
  public static <R extends MintTransactionReason> Token<R> create(
      RootTrustBase trustBase,
      TokenState state,
      MintTransaction<R> transaction,
      List<Token<?>> nametags
  ) throws VerificationException {
    Objects.requireNonNull(state, "State cannot be null");
    Objects.requireNonNull(transaction, "Genesis cannot be null");
    Objects.requireNonNull(trustBase, "Trust base cannot be null");
    Objects.requireNonNull(nametags, "Nametag tokens cannot be null");

    Token<R> token = new Token<>(state, transaction, List.of(), nametags);
    VerificationResult result = token.verify(trustBase);
    if (!result.isSuccessful()) {
        System.out.println(result);
      throw new VerificationException("Token verification failed", result);
    }

    return token;
  }

  /**
   * Update token to next state with given transfer transaction.
   *
   * @param trustBase   trust base to verify latest state
   * @param state       current state
   * @param transaction latest transaction
   * @param nametags    nametags associated with transaction
   * @return tokest with latest state
   * @throws VerificationException if token state is invalid
   */
  public Token<R> update(
      RootTrustBase trustBase,
      TokenState state,
      TransferTransaction transaction,
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

    LinkedList<TransferTransaction> transactions = new LinkedList<>(this.transactions);
    transactions.add(transaction);
      Token token = new Token<>(state, this.genesis, transactions, nametags);
      VerificationResult result2 = token.verify(trustBase);
      if (!result2.isSuccessful()) {
          throw new VerificationException("Token verification failed", result2);
      }

      return token;
  }

  /**
   * Verify current token state against trustbase.
   *
   * @param trustBase trust base to verify state against
   * @return verification result
   */
  public VerificationResult verify(RootTrustBase trustBase) {
    List<VerificationResult> results = new ArrayList<>();
    results.add(
        VerificationResult.fromChildren(
            "Genesis verification",
            List.of(Token.verifyGenesis(this.genesis, trustBase)))
    );

    for (int i = 0; i < this.transactions.size(); i++) {
      TransferTransaction transaction = this.transactions.get(i);

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
      TransferTransaction transaction,
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

    if (!previousTransaction.containsRecipientDataHash(
        token.getState().getData().orElse(null))
    ) {
      return VerificationResult.fail("data mismatch");
    }

    if (transaction != null && !predicate.verify(token, transaction, trustBase)) {
      return VerificationResult.fail("predicate verification failed");
    }

    return VerificationResult.success();
  }

  private static VerificationResult verifyGenesis(
      MintTransaction<?> transaction,
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

    RequestId requestId = RequestId.create(
        signingService.getPublicKey(),
        transaction.getData().getSourceState()
    );
    if (transaction.getInclusionProof().verify(requestId, trustBase)
        != InclusionProofVerificationStatus.OK) {
      return VerificationResult.fail("Inclusion proof verification failed.");
    }

    return VerificationResult.success();
  }

  /**
   * Create token from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return token
   */
  public static Token<?> fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);
    String version = CborDeserializer.readTextString(data.get(0));
    if (!Token.TOKEN_VERSION.equals(version)) {
      throw new CborSerializationException("Invalid version: " + version);
    }

    return new Token<>(
        TokenState.fromCbor(data.get(1)),
        MintTransaction.fromCbor(data.get(2)),
        CborDeserializer.readArray(data.get(3)).stream()
            .map(TransferTransaction::fromCbor)
            .collect(Collectors.toList()),
        CborDeserializer.readArray(data.get(4)).stream()
            .map(Token::fromCbor)
            .collect(Collectors.toList())
    );
  }

  /**
   * Convert token to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        CborSerializer.encodeTextString(TOKEN_VERSION),
        this.state.toCbor(),
        this.genesis.toCbor(),
        CborSerializer.encodeArray(
            this.transactions.stream()
                .map(TransferTransaction::toCbor)
                .toArray(byte[][]::new)
        ),
        CborSerializer.encodeArray(
            this.nametags.stream()
                .map(Token::toCbor)
                .toArray(byte[][]::new)
        )
    );
  }

  /**
   * Create token from JSON string.
   *
   * @param input JSON string
   * @return token
   */
  public static Token<?> fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, Token.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(Token.class, e);
    }
  }

  /**
   * Convert token to JSON string.
   *
   * @return JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(Token.class, e);
    }
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