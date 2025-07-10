package com.unicity.sdk.serializer.cbor.transaction;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.serializer.cbor.token.TokenCborSerializer;
import com.unicity.sdk.shared.cbor.CborDecoder;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.cbor.CustomCborDecoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.MintTransactionData;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A serializer for {@link MintTransactionData} objects using CBOR encoding.
 * Handles serialization and deserialization of mint transaction data for tokens.
 */
public class MintTransactionDataCborSerializer {
    private final TokenCborSerializer tokenSerializer;

    /**
     * Constructs a new MintTransactionDataCborSerializer.
     * @param tokenSerializer Token serializer used for token-specific deserialization.
     */
    public MintTransactionDataCborSerializer(TokenCborSerializer tokenSerializer) {
        this.tokenSerializer = tokenSerializer;
    }

    /**
     * Serializes MintTransactionData into a CBOR-encoded byte array.
     * @param data The MintTransactionData to serialize.
     * @return CBOR-encoded byte array.
     */
    public static byte[] serialize(MintTransactionData data) {
        return CborEncoder.encodeArray(
            data.getTokenId().toCBOR(),
            data.getTokenType().toCBOR(),
            CborEncoder.encodeByteString(data.getTokenData().toCBOR()),
            data.getCoinData() != null ? data.getCoinData().toCBOR() : CborEncoder.encodeNull(),
            CborEncoder.encodeTextString(data.getRecipient().getAddress()),
            CborEncoder.encodeByteString(data.getSalt()),
            data.getDataHash() != null ? data.getDataHash().toCBOR() : CborEncoder.encodeNull(),
            data.getReason() != null ? ((ISerializable) data.getReason()).toCBOR() : CborEncoder.encodeNull()
        );
    }

    /**
     * Deserializes a CBOR-encoded byte array into MintTransactionData.
     * @param bytes The CBOR-encoded data.
     * @return A CompletableFuture resolving to the deserialized MintTransactionData.
     */
    public CompletableFuture<MintTransactionData> deserialize(byte[] bytes) {
        try {
            CustomCborDecoder.DecodeResult result = CustomCborDecoder.decode(bytes, 0);
            
            if (!(result.value instanceof List)) {
                return CompletableFuture.failedFuture(new RuntimeException("Expected array for MintTransactionData"));
            }
            
            List<?> data = (List<?>) result.value;
            if (data.size() < 8) {
                return CompletableFuture.failedFuture(new RuntimeException("Invalid MintTransactionData array size"));
            }
            
            // Deserialize components
            TokenId tokenId = TokenId.create((byte[]) data.get(0));
            TokenType tokenType = TokenType.create((byte[]) data.get(1));
            byte[] tokenData = (byte[]) data.get(2);
            
            TokenCoinData coinData = null;
            if (data.get(3) != null && data.get(3) instanceof byte[]) {
                coinData = TokenCoinData.fromCBOR((byte[]) data.get(3));
            }
            
            String recipient = (String) data.get(4);
            byte[] salt = (byte[]) data.get(5);
            
            DataHash dataHash = null;
            if (data.get(6) != null && data.get(6) instanceof byte[]) {
                dataHash = DataHash.fromCBOR((byte[]) data.get(6));
            }
            
            // TODO: Handle mint reason deserialization
            // TODO: Handle predicate deserialization
            // For now, create without reason and use null predicate
            return CompletableFuture.completedFuture(
                new MintTransactionData(
                    tokenId,
                    tokenType,
                    null, // predicate - TODO: deserialize from tokenData
                    null, // tokenData - TODO: deserialize based on token type
                    coinData,
                    dataHash,
                    salt,
                    null // reason
                )
            );
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}