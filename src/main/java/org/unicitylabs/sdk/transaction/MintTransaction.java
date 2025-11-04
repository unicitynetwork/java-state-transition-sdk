
package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.address.AddressFactory;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.split.SplitMintReason;
import org.unicitylabs.sdk.util.HexConverter;
import org.unicitylabs.sdk.verification.VerificationResult;


/**
 * Mint transaction.
 *
 * @param <R> mint reason
 */
public class MintTransaction<R extends MintTransactionReason> extends
    Transaction<MintTransaction.Data<R>> {

  @JsonCreator
  MintTransaction(
      @JsonProperty("data")
      Data<R> data,
      @JsonProperty("inclusionProof")
      InclusionProof inclusionProof) {
    super(data, inclusionProof);
  }

  /**
   * Create mint transaction from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return mint transaction
   */
  public static MintTransaction<?> fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new MintTransaction<>(
        Data.fromCbor(data.get(0)),
        InclusionProof.fromCbor(data.get(1))
    );
  }

  /**
   * Create mint transaction from JSON string.
   *
   * @param input JSON string
   * @return mint transaction
   */
  public static MintTransaction<?> fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, MintTransaction.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(MintTransaction.class, e);
    }
  }

  /**
   * Verify mint transaction.
   *
   * @param trustBase root trust base
   * @return verification result
   */
  public VerificationResult verify(RootTrustBase trustBase) {
    if (!this.getInclusionProof().getAuthenticator().isPresent()) {
      return VerificationResult.fail("Missing authenticator");
    }

    if (!this.getInclusionProof().getTransactionHash().isPresent()) {
      return VerificationResult.fail("Missing transaction hash");
    }

    if (!this.getData().getSourceState()
        .equals(MintTransactionState.create(this.getData().getTokenId()))) {
      return VerificationResult.fail("Invalid source state");
    }

    SigningService signingService = MintCommitment.createSigningService(this.getData());
    if (!Arrays.equals(signingService.getPublicKey(),
        this.getInclusionProof().getAuthenticator().get().getPublicKey())) {
      return VerificationResult.fail("Authenticator public key mismatch");
    }

    if (!this.getInclusionProof().getAuthenticator().get()
        .verify(this.getInclusionProof().getTransactionHash().get())) {
      return VerificationResult.fail("Authenticator verification failed");
    }

    VerificationResult reasonResult = this.getData().getReason()
        .map(reason -> reason.verify(this))
        .orElse(VerificationResult.success());

    if (!reasonResult.isSuccessful()) {
      return VerificationResult.fail(
          "Mint reason verification failed", Collections.singletonList(reasonResult));
    }

    InclusionProofVerificationStatus inclusionProofStatus = this.getInclusionProof().verify(
        RequestId.create(signingService.getPublicKey(), this.getData().getSourceState()),
        trustBase
    );

    if (inclusionProofStatus != InclusionProofVerificationStatus.OK) {
      return VerificationResult.fail("Inclusion proof verification failed");
    }

    return VerificationResult.success();
  }

  /**
   * Mint transaction data.
   *
   * @param <R> mint reason
   */
  public static class Data<R extends MintTransactionReason> implements
      TransactionData<MintTransactionState> {

    private final TokenId tokenId;
    private final TokenType tokenType;
    private final byte[] tokenData;
    private final TokenCoinData coinData;
    private final MintTransactionState sourceState;
    private final Address recipient;
    private final byte[] salt;
    private final DataHash recipientDataHash;
    private final R reason;

    /**
     * Create mint transaction data.
     *
     * @param tokenId           token id
     * @param tokenType         token type
     * @param tokenData         token immutable data
     * @param coinData          token coin data
     * @param recipient         token recipient address
     * @param salt              mint transaction salt
     * @param recipientDataHash recipient data hash
     * @param reason            mint reason
     */
    @JsonCreator
    public Data(
        @JsonProperty("tokenId") TokenId tokenId,
        @JsonProperty("tokenType") TokenType tokenType,
        @JsonProperty("tokenData") byte[] tokenData,
        @JsonProperty("coinData") TokenCoinData coinData,
        @JsonProperty("recipient") Address recipient,
        @JsonProperty("salt") byte[] salt,
        @JsonProperty("recipientDataHash") DataHash recipientDataHash,
        @JsonProperty("reason")
        @JsonDeserialize(using = MintTransactionReasonJson.Deserializer.class)
        R reason
    ) {
      Objects.requireNonNull(tokenId, "Token ID cannot be null");
      Objects.requireNonNull(tokenType, "Token type cannot be null");
      Objects.requireNonNull(recipient, "Recipient cannot be null");
      Objects.requireNonNull(salt, "Salt cannot be null");

      this.tokenId = tokenId;
      this.tokenType = tokenType;
      this.tokenData = tokenData == null ? null : Arrays.copyOf(tokenData, tokenData.length);
      this.coinData = coinData;
      this.sourceState = MintTransactionState.create(tokenId);
      this.recipient = recipient;
      this.salt = Arrays.copyOf(salt, salt.length);
      this.recipientDataHash = recipientDataHash;
      this.reason = reason;
    }

    /**
     * Get token id.
     *
     * @return token id
     */
    public TokenId getTokenId() {
      return this.tokenId;
    }

    /**
     * Get token type.
     *
     * @return token type
     */
    public TokenType getTokenType() {
      return this.tokenType;
    }

    /**
     * Get immutable token data.
     *
     * @return token data
     */
    public Optional<byte[]> getTokenData() {
      return Optional.ofNullable(this.tokenData);
    }

    /**
     * Get token coin data.
     *
     * @return token coin data
     */
    public Optional<TokenCoinData> getCoinData() {
      return Optional.ofNullable(this.coinData);
    }

    /**
     * Get recipient data hash.
     *
     * @return recipient data hash
     */
    public Optional<DataHash> getRecipientDataHash() {
      return Optional.ofNullable(this.recipientDataHash);
    }

    /**
     * Get mint transaction salt.
     *
     * @return transaction salt
     */
    public byte[] getSalt() {
      return Arrays.copyOf(this.salt, this.salt.length);
    }

    /**
     * Get token recipient address.
     *
     * @return recipient address
     */
    public Address getRecipient() {
      return this.recipient;
    }

    /**
     * Get mint reason.
     *
     * @return mint reason
     */
    public Optional<R> getReason() {
      return Optional.ofNullable(this.reason);
    }

    /**
     * Get mint transaction source state.
     *
     * @return source state
     */
    @JsonIgnore
    public MintTransactionState getSourceState() {
      return this.sourceState;
    }

    /**
     * Calculate mint transaction hash.
     *
     * @return transaction hash.
     */
    public DataHash calculateHash() {
      return new DataHasher(HashAlgorithm.SHA256)
          .update(this.toCbor())
          .digest();
    }

    /**
     * Create mint transaction data from CBOR bytes.
     *
     * @param bytes CBOR bytes
     * @return mint transaction data
     */
    public static Data<?> fromCbor(byte[] bytes) {
      List<byte[]> data = CborDeserializer.readArray(bytes);

      return new Data<>(
          TokenId.fromCbor(data.get(0)),
          TokenType.fromCbor(data.get(1)),
          CborDeserializer.readOptional(data.get(2), CborDeserializer::readByteString),
          CborDeserializer.readOptional(data.get(3), TokenCoinData::fromCbor),
          AddressFactory.createAddress(CborDeserializer.readTextString(data.get(4))),
          CborDeserializer.readByteString(data.get(5)),
          CborDeserializer.readOptional(data.get(6), DataHash::fromCbor),
          CborDeserializer.readOptional(data.get(7), SplitMintReason::fromCbor)
      );
    }

    /**
     * Convert mint transaction data to CBOR bytes.
     *
     * @return CBOR bytes
     */
    public byte[] toCbor() {
      return CborSerializer.encodeArray(
          this.tokenId.toCbor(),
          this.tokenType.toCbor(),
          CborSerializer.encodeOptional(this.tokenData, CborSerializer::encodeByteString),
          CborSerializer.encodeOptional(this.coinData, TokenCoinData::toCbor),
          CborSerializer.encodeTextString(this.recipient.getAddress()),
          CborSerializer.encodeByteString(this.salt),
          CborSerializer.encodeOptional(this.recipientDataHash, DataHash::toCbor),
          CborSerializer.encodeOptional(this.reason, MintTransactionReason::toCbor)
      );
    }

    /**
     * Create mint transaction data from JSON string.
     *
     * @param input JSON string
     * @return mint transaction data
     */
    public static Data<?> fromJson(String input) {
      try {
        return UnicityObjectMapper.JSON.readValue(input, Data.class);
      } catch (JsonProcessingException e) {
        throw new JsonSerializationException(Data.class, e);
      }
    }

    /**
     * Convert mint transaction data to JSON string.
     *
     * @return JSON string
     */
    public String toJson() {
      try {
        return UnicityObjectMapper.JSON.writeValueAsString(this);
      } catch (JsonProcessingException e) {
        throw new JsonSerializationException(Data.class, e);
      }
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Data)) {
        return false;
      }
      Data<?> that = (Data<?>) o;

      return Objects.equals(this.tokenId, that.tokenId)
          && Objects.equals(this.tokenType, that.tokenType)
          && Objects.deepEquals(this.tokenData, that.tokenData)
          && Objects.equals(this.coinData, that.coinData)
          && Objects.equals(this.sourceState, that.sourceState)
          && Objects.equals(this.recipient, that.recipient)
          && Objects.deepEquals(this.salt, that.salt)
          && Objects.equals(this.recipientDataHash, that.recipientDataHash)
          && Objects.equals(this.reason, that.reason);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.tokenId, this.tokenType, Arrays.hashCode(tokenData), this.coinData,
          this.sourceState,
          this.recipient, Arrays.hashCode(this.salt), this.recipientDataHash, this.reason);
    }

    @Override
    public String toString() {
      return String.format(
          "Data{"
              + "tokenId=%s, "
              + "tokenType=%s, "
              + "tokenData=%s, "
              + "coinData=%s, "
              + "sourceState=%s, "
              + "recipient=%s, "
              + "salt=%s, "
              + "dataHash=%s, "
              + "reason=%s"
              + "}",
          this.tokenId, this.tokenType,
          this.tokenData != null ? HexConverter.encode(this.tokenData) : null, this.coinData,
          this.sourceState, this.recipient, HexConverter.encode(this.salt), this.recipientDataHash,
          this.reason);
    }
  }

  /**
   * Nametag mint data.
   */
  public static class NametagData extends Data<MintTransactionReason> {

    /**
     * Create nametag mint data.
     *
     * @param name          nametag
     * @param tokenType     token type
     * @param recipient     recipient address
     * @param salt          mint salt
     * @param targetAddress target address
     */
    public NametagData(
        String name,
        TokenType tokenType,
        Address recipient,
        byte[] salt,
        Address targetAddress
    ) {
      super(
          TokenId.fromNameTag(name),
          tokenType,
          targetAddress.getAddress().getBytes(StandardCharsets.UTF_8),
          null,
          recipient,
          salt,
          null,
          null
      );
    }
  }
}
