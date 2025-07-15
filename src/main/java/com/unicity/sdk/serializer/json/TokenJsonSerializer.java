package com.unicity.sdk.serializer.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.predicate.IPredicateFactory;
import com.unicity.sdk.serializer.json.token.TokenStateJsonSerializer;
import com.unicity.sdk.serializer.json.transaction.MintTransactionJsonSerializer;
import com.unicity.sdk.serializer.json.transaction.TransactionJsonSerializer;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferTransactionData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * JSON serializer for Token objects.
 */
public class TokenJsonSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final IPredicateFactory predicateFactory;
    private final MintTransactionJsonSerializer mintTransactionDeserializer;
    private final TransactionJsonSerializer transactionSerializer;
    private final TokenStateJsonSerializer stateSerializer;
    
    public TokenJsonSerializer(IPredicateFactory predicateFactory) {
        this.predicateFactory = predicateFactory;
        this.mintTransactionDeserializer = new MintTransactionJsonSerializer(this);
        this.transactionSerializer = new TransactionJsonSerializer(predicateFactory);
        this.stateSerializer = new TokenStateJsonSerializer(predicateFactory);
    }

    /**
     * Serializes a Token object into a JSON representation.
     * @param token The token to serialize
     * @return JSON representation of the token
     */
    public static Object serialize(Token<Transaction<MintTransactionData>> token) {
        ObjectNode result = objectMapper.createObjectNode();
        
        result.put("version", token.getVersion());
        result.set("genesis", objectMapper.valueToTree(MintTransactionJsonSerializer.serialize(token.getGenesis())));
        
        ArrayNode transactions = objectMapper.createArrayNode();
        for (Transaction<?> tx : token.getTransactions()) {
            @SuppressWarnings("unchecked")
            Transaction<TransferTransactionData> txData = (Transaction<TransferTransactionData>) tx;
            transactions.add(objectMapper.valueToTree(TransactionJsonSerializer.serialize(txData)));
        }
        result.set("transactions", transactions);
        
        result.set("state", objectMapper.valueToTree(TokenStateJsonSerializer.serialize(token.getState())));
        
        ArrayNode nametagTokens = objectMapper.createArrayNode();
        // TODO: Add nametag tokens when implemented
        result.set("nametagTokens", nametagTokens);
        
        return result;
    }
    
    /**
     * Deserializes a JSON representation of a token into a Token object.
     * @param data The JSON data to deserialize
     * @return A promise that resolves to the deserialized Token object
     */
    public CompletableFuture<Token<Transaction<MintTransactionData>>> deserialize(JsonNode data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String tokenVersion = data.get("version").asText();
                if (!Token.TOKEN_VERSION.equals(tokenVersion)) {
                    throw new IllegalArgumentException("Cannot parse token. Version mismatch: " + tokenVersion + " !== " + Token.TOKEN_VERSION);
                }
                
                JsonNode genesisNode = data.get("genesis");
                Transaction<MintTransactionData> mintTransaction = mintTransactionDeserializer.deserialize(genesisNode).get();
                
                List<Transaction<TransferTransactionData>> transactions = new ArrayList<>();
                JsonNode transactionsNode = data.get("transactions");
                if (transactionsNode != null && transactionsNode.isArray()) {
                    for (JsonNode txNode : transactionsNode) {
                        transactions.add(
                            transactionSerializer.deserialize(
                                mintTransaction.getData().getTokenId(),
                                mintTransaction.getData().getTokenType(),
                                txNode
                            ).get()
                        );
                    }
                }
                
                JsonNode stateNode = data.get("state");
                TokenState tokenState = stateSerializer.deserialize(
                    mintTransaction.getData().getTokenId(),
                    mintTransaction.getData().getTokenType(),
                    stateNode
                ).get();
                
                // TODO: Add nametag tokens
                List<Token<?>> nametagTokens = new ArrayList<>();
                
                return new Token<Transaction<MintTransactionData>>(
                    tokenState,
                    (Transaction) mintTransaction,
                    (List) transactions,
                    nametagTokens,
                    tokenVersion
                );
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize token", e);
            }
        });
    }
    
    public CompletableFuture<Token<?>> create(Object data) {
        if (data instanceof JsonNode) {
            return deserialize((JsonNode) data).thenApply(t -> t);
        }
        throw new IllegalArgumentException("Expected JsonNode but got " + data.getClass());
    }
}