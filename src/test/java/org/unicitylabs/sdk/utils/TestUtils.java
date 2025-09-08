package org.unicitylabs.sdk.utils;

import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

/**
 * Utility methods for tests.
 */
public class TestUtils {
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Generate random bytes of specified length.
     */
    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
    
    /**
     * Generate a random coin amount between 10 and 99.
     */
    public static BigInteger randomCoinAmount() {
        return BigInteger.valueOf(10 + RANDOM.nextInt(90));
    }
    
    /**
     * Create random coin data with specified number of coins.
     */
    public static TokenCoinData randomCoinData(int numCoins) {
        Map<CoinId, BigInteger> coins = new java.util.HashMap<>();
        for (int i = 0; i < numCoins; i++) {
            coins.put(new CoinId(randomBytes(32)), randomCoinAmount());
        }
        return new TokenCoinData(coins);
    }
}