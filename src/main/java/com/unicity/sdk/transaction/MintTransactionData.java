package com.unicity.sdk.transaction;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.unicity.sdk.address.Address;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.token.NameTagTokenState;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.util.HexConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class MintTransactionData<R extends MintTransactionReason> implements
    TransactionData<RequestId> {

  private static final byte[] MINT_SUFFIX = HexConverter.decode(
      "9e82002c144d7c5796c50f6db50a0c7bbd7f717ae3af6c6c71a3e9eba3022730");

  private final TokenId tokenId;
  private final TokenType tokenType;
  private final byte[] tokenData;
  private final TokenCoinData coinData;
  private final RequestId sourceState;
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
    this.tokenData = Arrays.copyOf(tokenData, tokenData.length);
    this.coinData = coinData;
    this.sourceState = RequestId.createFromImprint(tokenId.getBytes(), MINT_SUFFIX);
    this.recipient = recipient;
    this.salt = Arrays.copyOf(salt, salt.length);
    this.dataHash = dataHash;
    this.reason = reason;
  }

  public static MintTransactionData<MintTransactionReason> createNametag(
      String name,
      TokenType tokenType,
      byte[] tokenData,
      TokenCoinData coinData,
      Address recipient,
      byte[] salt,
      Address targetAddress
  ) {
    Objects.requireNonNull(name, "Name cannot be null");
    Objects.requireNonNull(targetAddress, "Target address cannot be null");

    return new MintTransactionData<>(
        new TokenId(
            new DataHasher(HashAlgorithm.SHA256)
                .update(name.getBytes(StandardCharsets.UTF_8))
                .digest()
                .getImprint()
        ),
        tokenType,
        tokenData,
        coinData,
        recipient,
        salt,
        new DataHasher(HashAlgorithm.SHA256)
            .update(targetAddress.getAddress().getBytes(StandardCharsets.UTF_8))
            .digest(),
        null
    );
  }

  public TokenId getTokenId() {
    return this.tokenId;
  }

  public TokenType getTokenType() {
    return this.tokenType;
  }


  public byte[] getTokenData() {
    return this.tokenData;
  }

  public TokenCoinData getCoinData() {
    return this.coinData;
  }

  public DataHash getDataHash() {
    return this.dataHash;
  }

  public byte[] getSalt() {
    return Arrays.copyOf(this.salt, this.salt.length);
  }

  public Address getRecipient() {
    return this.recipient;
  }

  public R getReason() {
    return this.reason;
  }

  public RequestId getSourceState() {
    return this.sourceState;
  }

  public DataHash calculateHash() throws IOException {
    DataHash tokenDataHash = new DataHasher(HashAlgorithm.SHA256).update(tokenData).digest();
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.addPOJO(tokenId);
    node.addPOJO(tokenType);
    node.addPOJO(tokenDataHash);
    node.addPOJO(dataHash);
    node.addPOJO(coinData);
    node.add(recipient.getAddress());
    node.add(salt);
    node.addPOJO(reason);

    return new DataHasher(HashAlgorithm.SHA256).update(
        UnicityObjectMapper.CBOR.writeValueAsBytes(node)).digest();
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
        "MintTransactionData{tokenId=%s, tokenType=%s, tokenData=%s, coinData=%s, sourceState=%s, recipient=%s, salt=%s, dataHash=%s, reason=%s}",
        this.tokenId, this.tokenType, HexConverter.encode(this.tokenData), this.coinData,
        this.sourceState, this.recipient, HexConverter.encode(this.salt), this.dataHash,
        this.reason);
  }
}