package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.util.HexConverter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class MintTransactionData<R extends MintTransactionReason> implements
    TransactionData<MintTransactionState> {

  private final TokenId tokenId;
  private final TokenType tokenType;
  private final byte[] tokenData;
  private final TokenCoinData coinData;
  private final MintTransactionState sourceState;
  private final Address recipient;
  private final byte[] salt;
  private final DataHash dataHash;
  private final R reason;

  public MintTransactionData(
      TokenId tokenId,
      TokenType tokenType,
      byte[] tokenData,
      TokenCoinData coinData,
      Address recipient,
      byte[] salt,
      DataHash dataHash,
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
    this.dataHash = dataHash;
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

  public Optional<DataHash> getDataHash() {
    return Optional.ofNullable(this.dataHash);
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

  public MintTransactionState getSourceState() {
    return this.sourceState;
  }

  public DataHash calculateHash() {
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.addPOJO(this.tokenId);
    node.addPOJO(this.tokenType);
    node.addPOJO(this.tokenData);
    node.addPOJO(this.coinData);
    node.add(this.recipient.getAddress());
    node.add(this.salt);
    node.addPOJO(this.dataHash);
    node.addPOJO(this.reason);

    try {
      System.out.println(this.reason);
      return new DataHasher(HashAlgorithm.SHA256)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(node))
          .digest();
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MintTransactionData)) {
      return false;
    }
    MintTransactionData<?> that = (MintTransactionData<?>) o;

    return Objects.equals(this.tokenId, that.tokenId) && Objects.equals(this.tokenType,
        that.tokenType) && Objects.deepEquals(this.tokenData, that.tokenData)
        && Objects.equals(this.coinData, that.coinData) && Objects.equals(this.sourceState,
        that.sourceState) && Objects.equals(this.recipient, that.recipient)
        && Objects.deepEquals(this.salt, that.salt) && Objects.equals(this.dataHash,
        that.dataHash) && Objects.equals(this.reason, that.reason);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.tokenId, this.tokenType, Arrays.hashCode(tokenData), this.coinData,
        this.sourceState,
        this.recipient, Arrays.hashCode(this.salt), this.dataHash, this.reason);
  }

  @Override
  public String toString() {
    return String.format(
        "MintTransactionData{"
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
        this.sourceState, this.recipient, HexConverter.encode(this.salt), this.dataHash,
        this.reason);
  }
}