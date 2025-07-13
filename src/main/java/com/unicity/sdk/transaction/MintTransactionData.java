package com.unicity.sdk.transaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.predicate.IPredicate;
import com.unicity.sdk.predicate.PredicateFactory;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.cbor.JacksonCborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.JavaDataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.util.HexConverter;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Mint transaction data with full constructor matching TypeScript
 */
public class MintTransactionData<T extends ISerializable> implements ISerializable {
    private static final byte[] MINT_SUFFIX = HexConverter.decode("9e82002c144d7c5796c50f6db50a0c7bbd7f717ae3af6c6c71a3e9eba3022730");
    private final TokenId tokenId;
    private final TokenType tokenType;
    private final IPredicate predicate;
    private final T tokenData;
    private final TokenCoinData coinData;
    private final DataHash dataHash;  // Optional data hash field
    private final byte[] salt;
    private final DirectAddress recipient;
    private final Object reason;  // Optional reason field
    private final DataHash hash; // Hash of the encoded transaction
    private final RequestId sourceState;

    /**
     * Constructor matching TypeScript implementation
     */
    public MintTransactionData(
            TokenId tokenId,
            TokenType tokenType,
            IPredicate predicate,
            T tokenData,
            TokenCoinData coinData,
            DataHash dataHash,
            byte[] salt) {
        this(tokenId, tokenType, predicate, tokenData, coinData, dataHash, salt, null);
    }
    
    public MintTransactionData(
            TokenId tokenId,
            TokenType tokenType,
            IPredicate predicate,
            T tokenData,
            TokenCoinData coinData,
            DataHash dataHash,
            byte[] salt,
            Object reason) {
        this.tokenId = tokenId;
        this.tokenType = tokenType;
        this.predicate = predicate;
        this.tokenData = tokenData;
        this.coinData = coinData;
        this.dataHash = dataHash;
        this.salt = Arrays.copyOf(salt, salt.length);
        this.reason = reason;
        
        // Calculate recipient from predicate
        try {
            this.recipient = DirectAddress.create(predicate.getReference()).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create recipient address", e);
        }
        
        // Create sourceState like TypeScript: RequestId.createFromImprint(tokenId.bytes, MINT_SUFFIX)
        try {
            this.sourceState = RequestId.createFromImprint(tokenId.getBytes(), MINT_SUFFIX).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create source state", e);
        }
        
        // Calculate hash
        this.hash = calculateHash();
    }
    
    private DataHash calculateHash() {
        try {
            // Hash calculation is different from CBOR encoding!
            // It uses tokenDataHash instead of raw tokenData
            JavaDataHasher tokenDataHasher = new JavaDataHasher(HashAlgorithm.SHA256);
            tokenDataHasher.update(tokenData.toCBOR());
            DataHash tokenDataHash = tokenDataHasher.digest().get();
            
            JavaDataHasher hasher = new JavaDataHasher(HashAlgorithm.SHA256);
            hasher.update(CborEncoder.encodeArray(
                tokenId.toCBOR(),
                tokenType.toCBOR(),
                tokenDataHash.toCBOR(),  // Hash of token data, not the data itself
                dataHash != null ? dataHash.toCBOR() : CborEncoder.encodeNull(),
                coinData != null ? coinData.toCBOR() : CborEncoder.encodeNull(),
                CborEncoder.encodeTextString(recipient.getAddress()),
                CborEncoder.encodeByteString(salt),
                reason != null ? CborEncoder.encodeByteString((byte[]) reason) : CborEncoder.encodeNull()
            ));
            return hasher.digest().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hash", e);
        }
    }

    public TokenId getTokenId() {
        return tokenId;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public IPredicate getPredicate() {
        return predicate;
    }

    public T getTokenData() {
        return tokenData;
    }

    public TokenCoinData getCoinData() {
        return coinData;
    }

    public DataHash getDataHash() {
        return dataHash;
    }
    
    public Object getReason() {
        return reason;
    }

    public byte[] getSalt() {
        return Arrays.copyOf(salt, salt.length);
    }

    public DirectAddress getRecipient() {
        return recipient;
    }

    public DataHash getHash() {
        return hash;
    }
    
    public RequestId getSourceState() {
        return sourceState;
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        
        root.put("tokenId", tokenId.toJSON());
        root.put("tokenType", tokenType.toJSON());
        root.set("unlockPredicate", mapper.valueToTree(predicate.toJSON()));
        root.set("tokenData", mapper.valueToTree(tokenData.toJSON()));
        if (coinData != null) {
            root.set("coinData", mapper.valueToTree(coinData.toJSON()));
        }
        if (dataHash != null) {
            root.set("dataHash", mapper.valueToTree(dataHash.toJSON()));
        }
        root.put("salt", HexConverter.encode(salt));
        root.set("recipient", mapper.valueToTree(recipient.toJSON()));
        if (reason != null) {
            root.set("reason", mapper.valueToTree(reason));
        }
        
        return root;
    }

    @Override
    public byte[] toCBOR() {
        // Match TypeScript SDK structure exactly
        return CborEncoder.encodeArray(
            tokenId.toCBOR(),                                              // Field 0: Token ID
            tokenType.toCBOR(),                                            // Field 1: Token Type
            CborEncoder.encodeByteString(tokenData.toCBOR()),             // Field 2: Token Data as byte string
            coinData != null ? coinData.toCBOR() : CborEncoder.encodeNull(), // Field 3: Coin Data or null
            CborEncoder.encodeTextString(recipient.getAddress()),         // Field 4: Recipient as text string
            CborEncoder.encodeByteString(salt),                           // Field 5: Salt
            dataHash != null ? dataHash.toCBOR() : CborEncoder.encodeNull(), // Field 6: Data Hash or null
            reason != null ? CborEncoder.encodeByteString((byte[]) reason) : CborEncoder.encodeNull() // Field 7: Reason or null
        );
    }
    
    /**
     * Deserialize MintTransactionData from JSON.
     * @param jsonNode JSON node containing mint transaction data
     * @return MintTransactionData instance
     */
    public static MintTransactionData<?> fromJSON(JsonNode jsonNode) throws Exception {
        // Deserialize token ID
        String tokenIdHex = jsonNode.get("tokenId").asText();
        TokenId tokenId = TokenId.create(HexConverter.decode(tokenIdHex));
        
        // Deserialize token type
        String tokenTypeHex = jsonNode.get("tokenType").asText();
        TokenType tokenType = TokenType.create(HexConverter.decode(tokenTypeHex));
        
        // Deserialize predicate
        JsonNode predicateNode = jsonNode.get("predicate");
        IPredicate predicate = PredicateFactory.fromJSON(predicateNode);
        
        // Deserialize token data - we'll use a simple wrapper for raw bytes
        ISerializable tokenData = null;
        if (jsonNode.has("tokenData") && !jsonNode.get("tokenData").isNull()) {
            String tokenDataHex = jsonNode.get("tokenData").asText();
            final byte[] tokenDataBytes = HexConverter.decode(tokenDataHex);
            
            // Create a simple ISerializable wrapper for the token data
            tokenData = new ISerializable() {
                @Override
                public Object toJSON() {
                    return HexConverter.encode(tokenDataBytes);
                }
                
                @Override
                public byte[] toCBOR() {
                    return CborEncoder.encodeByteString(tokenDataBytes);
                }
                
                public byte[] getData() {
                    return tokenDataBytes;
                }
            };
        }
        
        // Deserialize coin data
        TokenCoinData coinData = null;
        if (jsonNode.has("coinData") && !jsonNode.get("coinData").isNull()) {
            JsonNode coinDataNode = jsonNode.get("coinData");
            coinData = TokenCoinData.fromJSON(coinDataNode);
        }
        
        // Deserialize salt
        String saltHex = jsonNode.get("salt").asText();
        byte[] salt = HexConverter.decode(saltHex);
        
        // Deserialize data hash (optional)
        DataHash dataHash = null;
        if (jsonNode.has("dataHash") && !jsonNode.get("dataHash").isNull()) {
            String dataHashHex = jsonNode.get("dataHash").asText();
            dataHash = DataHash.fromJSON(dataHashHex);
        }
        
        return new MintTransactionData<>(
            tokenId,
            tokenType,
            predicate,
            tokenData,
            coinData,
            dataHash,
            salt
        );
    }
}