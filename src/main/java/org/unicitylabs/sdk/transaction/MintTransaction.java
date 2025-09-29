
package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.address.AddressFactory;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.split.SplitMintReason;
import org.unicitylabs.sdk.util.HexConverter;


public class MintTransaction<R extends MintTransactionReason> extends
    Transaction<MintTransaction.Data<R>> {

  @JsonCreator
  public MintTransaction(
      @JsonProperty("data")
      Data<R> data,
      @JsonProperty("inclusionProof")
      InclusionProof inclusionProof) {
    super(data, inclusionProof);
  }

  public static MintTransaction<?> fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new MintTransaction<>(
        Data.fromCbor(data.get(0)),
        InclusionProof.fromCbor(data.get(1))
    );
  }

  public static MintTransaction<?> fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, MintTransaction.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(MintTransaction.class, e);
    }
  }

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

    @JsonCreator
    public Data(
        @JsonProperty("tokenId") TokenId tokenId,
        @JsonProperty("tokenType") TokenType tokenType,
        @JsonProperty("tokenData") byte[] tokenData,
        @JsonProperty("coinData") TokenCoinData coinData,
        @JsonProperty("recipient") Address recipient,
        @JsonProperty("salt") byte[] salt,
        @JsonProperty("recipientDataHash") DataHash dataHash,
        @JsonProperty("reason") R reason
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
      this.recipientDataHash = dataHash;
      this.reason = reason;
    }

    public TokenId getTokenId() {
      return this.tokenId;
    }

    public TokenType getTokenType() {
      return this.tokenType;
    }


    public Optional<byte[]> getTokenData() {
      return Optional.ofNullable(this.tokenData);
    }

    public Optional<TokenCoinData> getCoinData() {
      return Optional.ofNullable(this.coinData);
    }

    public Optional<DataHash> getRecipientDataHash() {
      return Optional.ofNullable(this.recipientDataHash);
    }

    public byte[] getSalt() {
      return Arrays.copyOf(this.salt, this.salt.length);
    }

    public Address getRecipient() {
      return this.recipient;
    }

    public Optional<R> getReason() {
      return Optional.ofNullable(this.reason);
    }

    @JsonIgnore
    public MintTransactionState getSourceState() {
      return this.sourceState;
    }

    public DataHash calculateHash() {
      return new DataHasher(HashAlgorithm.SHA256)
          .update(this.toCbor())
          .digest();
    }

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

    public static Data fromJson(String input) {
      try {
        return UnicityObjectMapper.JSON.readValue(input, Data.class);
      } catch (JsonProcessingException e) {
        throw new JsonSerializationException(Data.class, e);
      }
    }

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
          this.tokenId, this.tokenType, HexConverter.encode(this.tokenData), this.coinData,
          this.sourceState, this.recipient, HexConverter.encode(this.salt), this.recipientDataHash,
          this.reason);
    }
  }
}
