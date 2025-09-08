
package org.unicitylabs.sdk.token.fungible;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class TokenCoinData {
    private final Map<CoinId, BigInteger> coins;

    public TokenCoinData(Map<CoinId, BigInteger> coins) {
        this.coins = Collections.unmodifiableMap(coins);
    }

    public Map<CoinId, BigInteger> getCoins() {
        return this.coins;
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
