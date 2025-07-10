package com.unicity.sdk.serializer.cbor.transaction;

import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.predicate.IPredicateFactory;
import com.unicity.sdk.shared.cbor.CborDecoder;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.cbor.CustomCborDecoder;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.TransactionData;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A serializer for {@link Commitment} objects using CBOR encoding.
 * Handles serialization and deserialization of commitments, including their associated transaction data.
 */
public class CommitmentCborSerializer {
    private final TransactionDataCborSerializer transactionDataSerializer;

    /**
     * Constructs a new CommitmentCborSerializer instance.
     *
     * @param predicateFactory A factory for creating predicates used in transaction data deserialization.
     */
    public CommitmentCborSerializer(IPredicateFactory predicateFactory) {
        this.transactionDataSerializer = new TransactionDataCborSerializer(predicateFactory);
    }

    /**
     * Serializes a {@link Commitment} object into a CBOR-encoded byte array.
     *
     * @param commitment The commitment to serialize.
     * @return The CBOR-encoded representation of the commitment.
     */
    public static byte[] serialize(Commitment<TransactionData> commitment) {
        return CborEncoder.encodeArray(
            commitment.getRequestId().toCBOR(),
            TransactionDataCborSerializer.serialize(commitment.getTransactionData()),
            commitment.getAuthenticator().toCBOR()
        );
    }

    /**
     * Deserializes a CBOR-encoded byte array into a {@link Commitment} object.
     *
     * @param tokenId The ID of the token associated with the commitment.
     * @param tokenType The type of the token associated with the commitment.
     * @param bytes The CBOR-encoded data to deserialize.
     * @return A CompletableFuture that resolves to the deserialized Commitment object.
     */
    public CompletableFuture<Commitment> deserialize(
        TokenId tokenId,
        TokenType tokenType,
        byte[] bytes
    ) {
        try {
            CustomCborDecoder.DecodeResult result = CustomCborDecoder.decode(bytes, 0);
            
            if (!(result.value instanceof List)) {
                return CompletableFuture.failedFuture(new RuntimeException("Expected array for Commitment"));
            }
            
            List<?> data = (List<?>) result.value;
            if (data.size() < 3) {
                return CompletableFuture.failedFuture(new RuntimeException("Invalid Commitment array size"));
            }
            
            // Deserialize requestId
            RequestId requestId = RequestId.fromCBOR((byte[]) data.get(0));
            
            // Deserialize transaction data
            return transactionDataSerializer.deserialize(tokenId, tokenType, (byte[]) data.get(1))
                .thenApply(transactionData -> {
                    // Deserialize authenticator
                    Authenticator authenticator = Authenticator.fromCBOR((byte[]) data.get(2));
                    
                    return new Commitment(requestId, transactionData, authenticator);
                });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}