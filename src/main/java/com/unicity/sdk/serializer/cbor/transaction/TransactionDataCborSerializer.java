package com.unicity.sdk.serializer.cbor.transaction;

import com.unicity.sdk.predicate.IPredicateFactory;
import com.unicity.sdk.serializer.cbor.token.TokenStateCborSerializer;
import com.unicity.sdk.shared.cbor.CborDecoder;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.cbor.CustomCborDecoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.token.NameTagToken;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.TransactionData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A serializer for {@link TransactionData} objects using CBOR encoding.
 * Handles serialization and deserialization of transaction data.
 */
public class TransactionDataCborSerializer {
    private final TokenStateCborSerializer tokenStateSerializer;

    /**
     * Constructs a new TransactionDataCborSerializer instance.
     *
     * @param predicateFactory A factory for creating predicates used in transaction data deserialization.
     */
    public TransactionDataCborSerializer(IPredicateFactory predicateFactory) {
        this.tokenStateSerializer = new TokenStateCborSerializer(predicateFactory);
    }

    /**
     * Serializes a TransactionData object into a CBOR-encoded byte array.
     *
     * @param data The transaction data to serialize.
     * @return The CBOR-encoded representation of the transaction data.
     */
    public static byte[] serialize(TransactionData data) {
        // Serialize nametag tokens
        List<byte[]> nametagTokenBytes = new ArrayList<>();
        for (NameTagToken nametagToken : data.getNametagTokens()) {
            nametagTokenBytes.add(nametagToken.toCBOR());
        }

        return CborEncoder.encodeArray(
            TokenStateCborSerializer.serialize(data.getSourceState()),
            CborEncoder.encodeTextString(data.getRecipient()),
            CborEncoder.encodeByteString(data.getSalt()),
            data.getData() != null ? data.getData().toCBOR() : CborEncoder.encodeNull(),
            data.getMessage() != null ? CborEncoder.encodeByteString(data.getMessage()) : CborEncoder.encodeNull(),
            CborEncoder.encodeArray(nametagTokenBytes.toArray(new byte[0][]))
        );
    }

    /**
     * Deserializes a CBOR-encoded byte array into a TransactionData object.
     *
     * @param tokenId The ID of the token associated with the transaction data.
     * @param tokenType The type of the token associated with the transaction data.
     * @param bytes The CBOR-encoded data to deserialize.
     * @return A CompletableFuture that resolves to the deserialized TransactionData object.
     */
    public CompletableFuture<TransactionData> deserialize(TokenId tokenId, TokenType tokenType, byte[] bytes) {
        try {
            CustomCborDecoder.DecodeResult result = CustomCborDecoder.decode(bytes, 0);
            
            if (!(result.value instanceof List)) {
                return CompletableFuture.failedFuture(new RuntimeException("Expected array for TransactionData"));
            }
            
            List<?> data = (List<?>) result.value;
            if (data.size() < 6) {
                return CompletableFuture.failedFuture(new RuntimeException("Invalid TransactionData array size"));
            }
            
            // Deserialize source state
            return tokenStateSerializer.deserialize(tokenId, tokenType, (byte[]) data.get(0))
                .thenCompose(sourceState -> {
                    String recipient = (String) data.get(1);
                    byte[] salt = (byte[]) data.get(2);
                    
                    DataHash dataHash = null;
                    if (data.get(3) != null && data.get(3) instanceof byte[]) {
                        dataHash = DataHash.fromCBOR((byte[]) data.get(3));
                    }
                    
                    byte[] message = null;
                    if (data.get(4) != null && data.get(4) instanceof byte[]) {
                        message = (byte[]) data.get(4);
                    }
                    
                    // TODO: Deserialize nametag tokens
                    List<NameTagToken> nametagTokens = new ArrayList<>();
                    
                    return TransactionData.create(
                        sourceState,
                        recipient,
                        salt,
                        dataHash,
                        message,
                        nametagTokens
                    );
                });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}