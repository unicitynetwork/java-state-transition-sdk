
package com.unicity.sdk.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.unicity.sdk.address.Address;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.serializer.cbor.CborSerializationException;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.util.HexConverter;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Transaction data for token state transitions
 */
public class TransferTransactionData implements TransactionData<TokenState> {

  private final TokenState sourceState;
  private final Address recipient;
  private final byte[] salt;
  private final DataHash dataHash;
  private final byte[] message;
  private final Map<Address, Token<?>> nametags;

  public TransferTransactionData(
      TokenState sourceState,
      Address recipient,
      byte[] salt,
      DataHash dataHash,
      byte[] message,
      Map<Address, Token<?>> nametags) {
    Objects.requireNonNull(sourceState, "SourceState cannot be null");
    Objects.requireNonNull(recipient, "Recipient cannot be null");
    Objects.requireNonNull(salt, "Salt cannot be null");
    Objects.requireNonNull(nametags, "Nametags cannot be null");

    this.sourceState = sourceState;
    this.recipient = recipient;
    this.salt = Arrays.copyOf(salt, salt.length);
    this.dataHash = dataHash;
    this.message = message != null ? Arrays.copyOf(message, message.length) : null;
    this.nametags = Map.copyOf(nametags);
  }

  public TokenState getSourceState() {
    return this.sourceState;
  }

  public Address getRecipient() {
    return this.recipient;
  }

  public byte[] getSalt() {
    return Arrays.copyOf(this.salt, this.salt.length);
  }

  public Optional<DataHash> getDataHash() {
    return Optional.ofNullable(this.dataHash);
  }

  public Optional<byte[]> getMessage() {
    return this.message != null
        ? Optional.of(Arrays.copyOf(this.message, this.message.length))
        : Optional.empty();
  }

  public Map<Address, Token<?>> getNametags() {
    return this.nametags;
  }

  public DataHash calculateHash(TokenId tokenId, TokenType tokenType) {
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.addPOJO(this.sourceState.calculateHash(tokenId, tokenType));
    node.addPOJO(this.dataHash);
    node.addPOJO(this.recipient);
    node.add(this.salt);
    node.add(this.message);

    try {
      return new DataHasher(HashAlgorithm.SHA256).update(
          UnicityObjectMapper.CBOR.writeValueAsBytes(node)).digest();
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TransferTransactionData)) {
      return false;
    }
    TransferTransactionData that = (TransferTransactionData) o;
    return Objects.equals(this.sourceState, that.sourceState) && Objects.equals(
        this.recipient, that.recipient) && Objects.deepEquals(this.salt, that.salt)
        && Objects.equals(this.dataHash, that.dataHash) && Objects.deepEquals(this.message,
        that.message) && Objects.equals(this.nametags, that.nametags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.sourceState, this.recipient, Arrays.hashCode(this.salt), this.dataHash,
        Arrays.hashCode(this.message), this.nametags);
  }

  @Override
  public String toString() {
    return String.format(
        "TransferTransactionData{"
            + "sourceState=%s, "
            + "recipient=%s, "
            + "salt=%s, "
            + "dataHash=%s, "
            + "message=%s, "
            + "nametagTokens=%s"
            + "}",
        this.sourceState, this.recipient, HexConverter.encode(this.salt), this.dataHash,
        this.message != null ? HexConverter.encode(this.message) : null, this.nametags);
  }
}
