
package com.unicity.sdk.token.fungible;

import com.unicity.sdk.ISerializable;

import java.math.BigInteger;
import java.util.Map;

public class TokenCoinData implements ISerializable {
    private final Map<CoinId, BigInteger> coins;

    public TokenCoinData(Map<CoinId, BigInteger> coins) {
        this.coins = coins;
    }

    public Map<CoinId, BigInteger> getCoins() {
        return coins;
    }

    @Override
    public Object toJSON() {
        return this;
    }

    @Override
    public byte[] toCBOR() {
        return new byte[0];
    }
}
