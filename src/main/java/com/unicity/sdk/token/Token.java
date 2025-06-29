package com.unicity.sdk.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Token<T extends Transaction<MintTransactionData<?>>> implements ISerializable {
    public static final String TOKEN_VERSION = "2.0";

    private final String version;
    private final TokenState state;
    private final T genesis;
    private final List<Transaction<?>> transactions;
    private final List<Token<?>> nametagTokens;

    public Token(TokenState state, T genesis) {
        this(state, genesis, new ArrayList<>(), new ArrayList<>());
    }

    public Token(TokenState state, T genesis, List<Transaction<?>> transactions, List<Token<?>> nametagTokens) {
        this.version = TOKEN_VERSION;
        this.state = state;
        this.genesis = genesis;
        this.transactions = transactions;
        this.nametagTokens = nametagTokens;
    }

    public TokenId getId() {
        return genesis.getData().getTokenId();
    }

    public TokenType getType() {
        return genesis.getData().getTokenType();
    }

    public ISerializable getData() {
        return genesis.getData().getTokenData();
    }

    public TokenCoinData getCoins() {
        return genesis.getData().getCoinData();
    }

    public TokenState getState() {
        return state;
    }

    public T getGenesis() {
        return genesis;
    }

    public List<Transaction<?>> getTransactions() {
        return transactions;
    }

    public List<Token<?>> getNametagTokens() {
        return nametagTokens;
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("version", version);
        root.set("state", mapper.valueToTree(state.toJSON()));
        root.set("genesis", mapper.valueToTree(genesis.toJSON()));
        root.putArray("transactions").addAll(transactions.stream().map(t -> (JsonNode) t.toJSON()).collect(Collectors.toList()));
        root.putArray("nametagTokens").addAll(nametagTokens.stream().map(t -> (JsonNode) t.toJSON()).collect(Collectors.toList()));
        return root;
    }

    @Override
    public byte[] toCBOR() {
        CBORFactory factory = new CBORFactory();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             CBORGenerator generator = factory.createGenerator(baos)) {
            generator.writeStartObject();
            generator.writeFieldName("version");
            generator.writeString(version);
            generator.writeFieldName("state");
            generator.writeBinary(state.toCBOR());
            generator.writeFieldName("genesis");
            generator.writeBinary(genesis.toCBOR());
            generator.writeFieldName("transactions");
            generator.writeStartArray();
            for (Transaction transaction : transactions) {
                generator.writeBinary(transaction.toCBOR());
            }
            generator.writeEndArray();
            generator.writeFieldName("nametagTokens");
            generator.writeStartArray();
            for (Token token : nametagTokens) {
                generator.writeBinary(token.toCBOR());
            }
            generator.writeEndArray();
            generator.writeEndObject();
            generator.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}