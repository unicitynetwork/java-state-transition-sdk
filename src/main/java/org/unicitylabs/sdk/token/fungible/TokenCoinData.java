
package org.unicitylabs.sdk.token.fungible;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.unicitylabs.sdk.hash.TokenCoinDataJson;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.BigIntegerConverter;

@JsonSerialize(using = TokenCoinDataJson.Serializer.class)
@JsonDeserialize(using = TokenCoinDataJson.Deserializer.class)
public class TokenCoinData {
  private final Map<CoinId, BigInteger> coins;

  public TokenCoinData(Map<CoinId, BigInteger> coins) {
    this.coins = Collections.unmodifiableMap(coins);
  }

  public Map<CoinId, BigInteger> getCoins() {
    return this.coins;
  }

  public static TokenCoinData fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    Map<CoinId, BigInteger> coins = new LinkedHashMap<>();
    for (byte[] coinBytes : data) {
      List<byte[]> coinData = CborDeserializer.readArray(coinBytes);
      CoinId coinId = new CoinId(CborDeserializer.readByteString(coinData.get(0)));

      if (coins.containsKey(coinId)) {
        throw new CborSerializationException(
            String.format("Duplicate coin ID in coin data: %s", coinId)
        );
      }

      BigInteger amount = BigIntegerConverter.decode(
          CborDeserializer.readByteString(coinData.get(1))
      );
      coins.put(coinId, amount);
    }

    return new TokenCoinData(coins);
  }

  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        this.coins.entrySet().stream()
            .map(entry -> CborSerializer.encodeArray(
                CborSerializer.encodeByteString(entry.getKey().getBytes()),
                CborSerializer.encodeByteString(
                    BigIntegerConverter.encode(entry.getValue())
                )
            ))
            .toArray(byte[][]::new)
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TokenCoinData)) {
      return false;
    }
    TokenCoinData that = (TokenCoinData) o;
    return Objects.equals(this.coins, that.coins);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.coins);
  }

  @Override
  public String toString() {
    return String.format("TokenCoinData{%s}", this.coins);
  }
}
