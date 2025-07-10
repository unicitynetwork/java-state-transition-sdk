
package com.unicity.sdk.token.fungible;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborDecoder;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.cbor.CustomCborDecoder;
import com.unicity.sdk.shared.util.HexConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TokenCoinData implements ISerializable {
    private final Map<CoinId, BigInteger> coins;

    public TokenCoinData(Map<CoinId, BigInteger> coins) {
        this.coins = coins;
    }

    public static TokenCoinData create(Map<CoinId, BigInteger> coins) {
        return new TokenCoinData(coins);
    }

    public Map<CoinId, BigInteger> getCoins() {
        return coins;
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode coinsNode = mapper.createObjectNode();
        
        for (Map.Entry<CoinId, BigInteger> entry : coins.entrySet()) {
            coinsNode.put(entry.getKey().toJSON().toString(), entry.getValue().toString());
        }
        
        ObjectNode root = mapper.createObjectNode();
        root.set("coins", coinsNode);
        return root;
    }

    @Override
    public byte[] toCBOR() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Encode as CBOR array of arrays [[coinId, balance], ...]
            // This matches the TypeScript SDK structure
            baos.write(CborEncoder.encodeArrayStart(coins.size()));
            
            for (Map.Entry<CoinId, BigInteger> entry : coins.entrySet()) {
                // Each entry is an array of [coinId, balance]
                baos.write(CborEncoder.encodeArray(
                    CborEncoder.encodeByteString(entry.getKey().getValue().toByteArray()), // CoinId as byte string
                    CborEncoder.encodeByteString(entry.getValue().toByteArray())           // Balance as byte string
                ));
            }
            
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode TokenCoinData to CBOR", e);
        }
    }
    
    /**
     * Deserialize TokenCoinData from JSON.
     * @param jsonNode JSON node containing coin data
     * @return TokenCoinData instance
     */
    public static TokenCoinData fromJSON(JsonNode jsonNode) {
        Map<CoinId, BigInteger> coins = new HashMap<>();
        
        JsonNode coinsNode = jsonNode.get("coins");
        Iterator<Map.Entry<String, JsonNode>> fields = coinsNode.fields();
        
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String coinIdHex = entry.getKey();
            String balanceStr = entry.getValue().asText();
            
            CoinId coinId = new CoinId(HexConverter.decode(coinIdHex));
            BigInteger balance = new BigInteger(balanceStr);
            
            coins.put(coinId, balance);
        }
        
        return new TokenCoinData(coins);
    }
    
    /**
     * Deserialize TokenCoinData from CBOR.
     * @param cbor The CBOR-encoded bytes
     * @return A TokenCoinData instance
     */
    public static TokenCoinData fromCBOR(byte[] cbor) {
        try {
            CustomCborDecoder.DecodeResult result = CustomCborDecoder.decode(cbor, 0);
            if (!(result.value instanceof List)) {
                throw new RuntimeException("Expected array for TokenCoinData");
            }
            
            List<?> coinArray = (List<?>) result.value;
            Map<CoinId, BigInteger> coins = new HashMap<>();
            
            for (Object coinEntry : coinArray) {
                if (!(coinEntry instanceof List)) {
                    throw new RuntimeException("Expected array for coin entry");
                }
                
                List<?> entry = (List<?>) coinEntry;
                if (entry.size() < 2) {
                    throw new RuntimeException("Invalid coin entry size");
                }
                
                // Decode coinId and balance from byte arrays
                byte[] coinIdBytes = (byte[]) entry.get(0);
                byte[] balanceBytes = (byte[]) entry.get(1);
                
                CoinId coinId = new CoinId(coinIdBytes);
                BigInteger balance = new BigInteger(1, balanceBytes);
                
                coins.put(coinId, balance);
            }
            
            return new TokenCoinData(coins);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize TokenCoinData from CBOR", e);
        }
    }
}
