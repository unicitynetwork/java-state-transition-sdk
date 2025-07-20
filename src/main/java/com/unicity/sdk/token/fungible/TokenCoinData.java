
package com.unicity.sdk.token.fungible;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

public class TokenCoinData {
    private final Map<CoinId, BigInteger> coins;

    private TokenCoinData(Map<CoinId, BigInteger> coins) {
        this.coins = coins;
    }

    public static TokenCoinData create(Map<CoinId, BigInteger> coins) {
        return new TokenCoinData(Map.copyOf(coins));
    }

    public Map<CoinId, BigInteger> getCoins() {
        return coins;
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
