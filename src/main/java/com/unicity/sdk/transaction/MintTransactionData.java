package com.unicity.sdk.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.predicate.IPredicate;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.JavaDataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.util.HexConverter;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;

import java.util.Arrays;

/**
 * Mint transaction data with full constructor matching TypeScript
 */
public class MintTransactionData<T extends ISerializable> implements ISerializable {
    private final TokenId tokenId;
    private final TokenType tokenType;
    private final IPredicate predicate;
    private final T tokenData;
    private final TokenCoinData coinData;
    private final byte[] data;
    private final byte[] salt;
    private final DirectAddress recipient;
    private DataHash hash;

    /**
     * Constructor matching TypeScript implementation
     */
    public MintTransactionData(
            TokenId tokenId,
            TokenType tokenType,
            IPredicate predicate,
            T tokenData,
            TokenCoinData coinData,
            byte[] data,
            byte[] salt) {
        this.tokenId = tokenId;
        this.tokenType = tokenType;
        this.predicate = predicate;
        this.tokenData = tokenData;
        this.coinData = coinData;
        this.data = Arrays.copyOf(data, data.length);
        this.salt = Arrays.copyOf(salt, salt.length);
        
        // Calculate recipient from predicate
        try {
            this.recipient = DirectAddress.create(predicate.getReference()).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create recipient address", e);
        }
        
        // Calculate hash
        this.hash = calculateHash();
    }
    
    private DataHash calculateHash() {
        try {
            JavaDataHasher hasher = new JavaDataHasher(HashAlgorithm.SHA256);
            hasher.update(toCBOR());
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

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
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

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        
        root.put("tokenId", tokenId.toJSON());
        root.put("tokenType", tokenType.toJSON());
        root.set("predicate", mapper.valueToTree(predicate.toJSON()));
        root.set("tokenData", mapper.valueToTree(tokenData.toJSON()));
        root.set("coinData", mapper.valueToTree(coinData.toJSON()));
        root.put("data", HexConverter.encode(data));
        root.put("salt", HexConverter.encode(salt));
        root.set("recipient", mapper.valueToTree(recipient.toJSON()));
        
        return root;
    }

    @Override
    public byte[] toCBOR() {
        return CborEncoder.encodeArray(
            tokenId.toCBOR(),
            tokenType.toCBOR(),
            predicate.toCBOR(),
            tokenData.toCBOR(),
            coinData.toCBOR(),
            CborEncoder.encodeByteString(data),
            CborEncoder.encodeByteString(salt),
            recipient.toCBOR()
        );
    }
}