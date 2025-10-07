
package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.address.AddressFactory;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Token transfer transaction.
 */
public class TransferTransaction extends Transaction<TransferTransaction.Data> {

  @JsonCreator
  TransferTransaction(
      @JsonProperty("data")
      Data data,
      @JsonProperty("inclusionProof")
      InclusionProof inclusionProof) {
    super(data, inclusionProof);
  }

  /**
   * Create transfer transaction from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return transfer transaction
   */
  public static TransferTransaction fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new TransferTransaction(
        Data.fromCbor(data.get(0)),
        InclusionProof.fromCbor(data.get(1))
    );
  }

  /**
   * Create transfer transaction from JSON string.
   *
   * @param input JSON string
   * @return transfer transaction
   */
  public static TransferTransaction fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, TransferTransaction.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(TransferTransaction.class, e);
    }
  }

  /**
   * Transaction data for token state transitions.
   */
  public static class Data implements TransactionData<TokenState> {

    private final TokenState sourceState;
    private final Address recipient;
    private final byte[] salt;
    private final DataHash recipientDataHash;
    private final byte[] message;
    private final List<Token<?>> nametags;

    @JsonCreator
    Data(
        @JsonProperty("sourceState") TokenState sourceState,
        @JsonProperty("recipient") Address recipient,
        @JsonProperty("salt") byte[] salt,
        @JsonProperty("recipientDataHash") DataHash recipientDataHash,
        @JsonProperty("message") byte[] message,
        @JsonProperty("nametags") List<Token<?>> nametags
    ) {
      Objects.requireNonNull(sourceState, "SourceState cannot be null");
      Objects.requireNonNull(recipient, "Recipient cannot be null");
      Objects.requireNonNull(salt, "Salt cannot be null");
      Objects.requireNonNull(nametags, "Nametags cannot be null");

      this.sourceState = sourceState;
      this.recipient = recipient;
      this.salt = Arrays.copyOf(salt, salt.length);
      this.recipientDataHash = recipientDataHash;
      this.message = message != null ? Arrays.copyOf(message, message.length) : null;
      this.nametags = List.copyOf(nametags);
    }

    /**
     * Get transaction source state.
     *
     * @return source state
     */
    public TokenState getSourceState() {
      return this.sourceState;
    }

    /**
     * Get transaction recipient address.
     *
     * @return recipient address
     */
    public Address getRecipient() {
      return this.recipient;
    }

    /**
     * Get transaction salt.
     *
     * @return transaction salt
     */
    public byte[] getSalt() {
      return Arrays.copyOf(this.salt, this.salt.length);
    }

    /**
     * Get transaction recipient data hash.
     *
     * @return recipient data hash
     */
    public Optional<DataHash> getRecipientDataHash() {
      return Optional.ofNullable(this.recipientDataHash);
    }

    /**
     * Get transaction message.
     *
     * @return transaction message
     */
    public Optional<byte[]> getMessage() {
      return this.message != null
          ? Optional.of(Arrays.copyOf(this.message, this.message.length))
          : Optional.empty();
    }

    /**
     * Get transaction nametags.
     *
     * @return nametags
     */
    public List<Token<?>> getNametags() {
      return this.nametags;
    }

    /**
     * Calculate transfer transaction data hash.
     *
     * @return transaction data hash
     */
    public DataHash calculateHash() {
      return new DataHasher(HashAlgorithm.SHA256)
          .update(this.toCbor())
          .digest();
    }

    /**
     * Create transfer transaction data from CBOR bytes.
     *
     * @param bytes CBOR bytes
     * @return transfer transaction
     */
    public static Data fromCbor(byte[] bytes) {
      List<byte[]> data = CborDeserializer.readArray(bytes);

      return new Data(
          TokenState.fromCbor(data.get(0)),
          AddressFactory.createAddress(CborDeserializer.readTextString(data.get(1))),
          CborDeserializer.readByteString(data.get(2)),
          CborDeserializer.readOptional(data.get(3), DataHash::fromCbor),
          CborDeserializer.readOptional(data.get(4), CborDeserializer::readByteString),
          CborDeserializer.readArray(data.get(5)).stream()
              .map(Token::fromCbor)
              .collect(Collectors.toList())
      );
    }

    /**
     * Convert transfer transaction data to CBOR bytes.
     *
     * @return CBOR bytes
     */
    public byte[] toCbor() {
      return CborSerializer.encodeArray(
          this.sourceState.toCbor(),
          CborSerializer.encodeTextString(this.recipient.getAddress()),
          CborSerializer.encodeByteString(this.salt),
          CborSerializer.encodeOptional(this.recipientDataHash, DataHash::toCbor),
          CborSerializer.encodeOptional(this.message, CborSerializer::encodeByteString),
          CborSerializer.encodeArray(
              this.nametags.stream()
                  .map(Token::toCbor)
                  .toArray(byte[][]::new)
          )
      );
    }

    /**
     * Create transfer transaction data from JSON string.
     *
     * @param input JSON string
     * @return transfer transaction data
     */
    public static Data fromJson(String input) {
      try {
        return UnicityObjectMapper.JSON.readValue(input, Data.class);
      } catch (JsonProcessingException e) {
        throw new JsonSerializationException(Data.class, e);
      }
    }

    /**
     * Convert transfer transaction data to JSON string.
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
      Data that = (Data) o;
      return Objects.equals(this.sourceState, that.sourceState)
          && Objects.equals(this.recipient, that.recipient)
          && Objects.deepEquals(this.salt, that.salt)
          && Objects.equals(this.recipientDataHash, that.recipientDataHash)
          && Objects.deepEquals(this.message, that.message)
          && Objects.equals(this.nametags, that.nametags);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.sourceState, this.recipient, Arrays.hashCode(this.salt),
          this.recipientDataHash,
          Arrays.hashCode(this.message), this.nametags);
    }

    @Override
    public String toString() {
      return String.format(
          "Data{"
              + "sourceState=%s, "
              + "recipient=%s, "
              + "salt=%s, "
              + "dataHash=%s, "
              + "message=%s, "
              + "nametags=%s"
              + "}",
          this.sourceState, this.recipient, HexConverter.encode(this.salt), this.recipientDataHash,
          this.message != null ? HexConverter.encode(this.message) : null, this.nametags);
    }
  }

}
