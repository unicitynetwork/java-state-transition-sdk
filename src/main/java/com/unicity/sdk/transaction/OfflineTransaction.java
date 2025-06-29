package com.unicity.sdk.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.util.HexConverter;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.predicate.IPredicate;
import com.unicity.sdk.predicate.PredicateFactory;
import com.unicity.sdk.token.fungible.TokenCoinData;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Represents a transaction with its commitment for offline processing.
 */
public class OfflineTransaction implements ISerializable {
    private final OfflineCommitment commitment;
    private final Token<Transaction<MintTransactionData<?>>> token;

    /**
     * @param commitment  The commitment for the transaction
     * @param token      The token being transferred
     */
    public OfflineTransaction(OfflineCommitment commitment, Token<Transaction<MintTransactionData<?>>> token) {
        if (commitment == null) {
            throw new IllegalArgumentException("Commitment cannot be null");
        }
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        this.commitment = commitment;
        this.token = token;
    }

    /**
     * Create OfflineTransaction from JSON data.
     * @param jsonString JSON string
     */
    public static CompletableFuture<OfflineTransaction> fromJSON(String jsonString) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(jsonString);
                
                if (!rootNode.has("commitment") || !rootNode.has("token")) {
                    throw new IllegalArgumentException("Invalid offline transaction JSON format");
                }
                
                JsonNode commitmentNode = rootNode.get("commitment");
                JsonNode tokenNode = rootNode.get("token");
                
                // Deserialize the token first to get tokenId and tokenType
                Token<Transaction<MintTransactionData<?>>> token = deserializeToken(tokenNode);
                
                // Deserialize the commitment with token context
                OfflineCommitment commitment = deserializeCommitment(commitmentNode, token.getId(), token.getType());
                
                return new OfflineTransaction(commitment, token);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize OfflineTransaction", e);
            }
        });
    }
    
    private static OfflineCommitment deserializeCommitment(JsonNode commitmentNode, TokenId tokenId, TokenType tokenType) throws Exception {
        // Deserialize RequestId
        String requestIdHex = commitmentNode.get("requestId").asText();
        RequestId requestId = RequestId.fromJSON(requestIdHex);
        
        // Deserialize TransactionData with token context
        JsonNode txDataNode = commitmentNode.get("transactionData");
        TransactionData transactionData = deserializeTransactionData(txDataNode, tokenId, tokenType);
        
        // Deserialize Authenticator
        JsonNode authNode = commitmentNode.get("authenticator");
        // Convert JsonNode to Map for Authenticator.fromJSON
        Map<String, Object> authMap = new HashMap<>();
        authMap.put("algorithm", authNode.get("algorithm").asText());
        authMap.put("publicKey", authNode.get("publicKey").asText());
        authMap.put("signature", authNode.get("signature").asText());
        authMap.put("stateHash", authNode.get("stateHash").asText());
        Authenticator authenticator = Authenticator.fromJSON(authMap);
        
        return new OfflineCommitment(requestId, transactionData, authenticator);
    }
    
    private static TransactionData deserializeTransactionData(JsonNode jsonNode, TokenId tokenId, TokenType tokenType) throws Exception {
        // Deserialize source state with token context
        JsonNode sourceStateNode = jsonNode.get("sourceState");
        TokenState sourceState = deserializeTokenState(sourceStateNode, tokenId, tokenType);
        
        // Get recipient
        String recipient = jsonNode.get("recipient").asText();
        
        // Get salt
        String saltHex = jsonNode.get("salt").asText();
        byte[] salt = HexConverter.decode(saltHex);
        
        // Get data hash
        JsonNode dataNode = jsonNode.get("data");
        DataHash data = DataHash.fromJSON(dataNode.asText());
        
        // Get message
        String messageHex = jsonNode.get("message").asText();
        byte[] message = HexConverter.decode(messageHex);
        
        return TransactionData.create(sourceState, recipient, salt, data, message).get();
    }
    
    private static Token<Transaction<MintTransactionData<?>>> deserializeToken(JsonNode tokenNode) throws Exception {
        // First, extract tokenId and tokenType from genesis transaction
        JsonNode genesisNode = tokenNode.get("genesis");
        JsonNode mintDataNode = genesisNode.get("data");
        
        String tokenIdHex = mintDataNode.get("tokenId").asText();
        String tokenTypeHex = mintDataNode.get("tokenType").asText();
        
        TokenId tokenId = TokenId.fromHex(tokenIdHex);
        TokenType tokenType = TokenType.fromHex(tokenTypeHex);
        
        // Now deserialize with token context
        return deserializeTokenWithContext(tokenNode, tokenId, tokenType);
    }
    
    private static Token<Transaction<MintTransactionData<?>>> deserializeTokenWithContext(
            JsonNode tokenNode, TokenId tokenId, TokenType tokenType) throws Exception {
        // Deserialize token state with context
        JsonNode stateNode = tokenNode.get("state");
        TokenState tokenState = deserializeTokenState(stateNode, tokenId, tokenType);
        
        // Deserialize genesis transaction with context
        JsonNode genesisNode = tokenNode.get("genesis");
        Transaction<MintTransactionData<?>> genesis = deserializeMintTransaction(genesisNode, tokenId, tokenType);
        
        return new Token<>(tokenState, genesis);
    }
    
    private static TokenState deserializeTokenState(JsonNode stateNode, TokenId tokenId, TokenType tokenType) throws Exception {
        // Get state data
        String dataHex = stateNode.get("data").asText();
        byte[] data = HexConverter.decode(dataHex);
        
        // Deserialize predicate with token context
        JsonNode predicateNode = stateNode.get("unlockPredicate");
        if (predicateNode == null) {
            throw new IllegalArgumentException("Missing 'unlockPredicate' in token state JSON");
        }
        IPredicate predicate = PredicateFactory.create(tokenId, tokenType, predicateNode).get();
        
        return TokenState.create(predicate, data);
    }
    
    private static Transaction<MintTransactionData<?>> deserializeMintTransaction(
            JsonNode txNode, TokenId tokenId, TokenType tokenType) throws Exception {
        // Deserialize transaction data
        JsonNode dataNode = txNode.get("data");
        MintTransactionData<?> mintData = deserializeMintTransactionData(dataNode, tokenId, tokenType);
        
        // Deserialize inclusion proof
        JsonNode proofNode = txNode.get("inclusionProof");
        InclusionProof inclusionProof = InclusionProof.fromJSON(proofNode);
        
        return new Transaction<>(mintData, inclusionProof);
    }
    
    private static MintTransactionData<?> deserializeMintTransactionData(
            JsonNode dataNode, TokenId tokenId, TokenType tokenType) throws Exception {
        // We already have tokenId and tokenType from earlier extraction
        
        // Deserialize unlock predicate with context
        JsonNode predicateNode = dataNode.get("unlockPredicate");
        IPredicate unlockPredicate = PredicateFactory.create(tokenId, tokenType, predicateNode).get();
        
        // Deserialize token data
        JsonNode tokenDataNode = dataNode.get("tokenData");
        ISerializable tokenData = null;
        if (tokenDataNode != null && !tokenDataNode.isNull()) {
            if (tokenDataNode.isTextual()) {
                // Direct hex string (e.g., from TestTokenData)
                byte[] data = HexConverter.decode(tokenDataNode.asText());
                tokenData = new SimpleTokenData(data);
            } else if (tokenDataNode.isObject() && tokenDataNode.has("data")) {
                // Object with a "data" field
                String tokenDataHex = tokenDataNode.get("data").asText();
                byte[] data = HexConverter.decode(tokenDataHex);
                tokenData = new SimpleTokenData(data);
            }
        }
        
        // Deserialize coin data
        JsonNode coinDataNode = dataNode.get("coinData");
        TokenCoinData coinData = null;
        if (coinDataNode != null && !coinDataNode.isNull()) {
            coinData = TokenCoinData.fromJSON(coinDataNode);
        }
        
        // Get other fields
        String saltHex = dataNode.get("salt").asText();
        byte[] salt = HexConverter.decode(saltHex);
        
        JsonNode dataHashNode = dataNode.get("dataHash");
        DataHash dataHash = null;
        if (dataHashNode != null && !dataHashNode.isNull()) {
            dataHash = DataHash.fromJSON(dataHashNode.asText());
        }
        
        return new MintTransactionData<>(
            tokenId,
            tokenType,
            unlockPredicate,
            tokenData,
            coinData,
            dataHash,
            salt
        );
    }

    public OfflineCommitment getCommitment() {
        return commitment;
    }

    public Token<Transaction<MintTransactionData<?>>> getToken() {
        return token;
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        
        root.set("commitment", mapper.valueToTree(commitment.toJSON()));
        root.set("token", mapper.valueToTree(token.toJSON()));
        
        return root;
    }

    /**
     * Serialize to JSON string.
     * @return JSON string representation
     */
    public String toJSONString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(toJSON());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }

    @Override
    public byte[] toCBOR() {
        return CborEncoder.encodeArray(
            CborEncoder.encodeArray(
                commitment.getRequestId().toCBOR(),
                commitment.getTransactionData().toCBOR(),
                commitment.getAuthenticator().toCBOR()
            ),
            token.toCBOR()
        );
    }
    
    /**
     * Simple wrapper for token data during deserialization.
     */
    private static class SimpleTokenData implements ISerializable {
        private final byte[] data;
        
        public SimpleTokenData(byte[] data) {
            this.data = Arrays.copyOf(data, data.length);
        }
        
        public byte[] getData() {
            return Arrays.copyOf(data, data.length);
        }
        
        @Override
        public Object toJSON() {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("data", HexConverter.encode(data));
            return node;
        }
        
        @Override
        public byte[] toCBOR() {
            return CborEncoder.encodeByteString(data);
        }
    }
}
