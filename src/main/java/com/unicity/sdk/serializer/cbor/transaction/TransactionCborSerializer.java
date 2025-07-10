package com.unicity.sdk.serializer.cbor.transaction;

import com.unicity.sdk.predicate.IPredicateFactory;
import com.unicity.sdk.shared.cbor.CborDecoder;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.cbor.CustomCborDecoder;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransactionData;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A serializer for {@link Transaction} containing {@link TransactionData} using CBOR encoding.
 * Handles serialization and deserialization of transactions.
 */
public class TransactionCborSerializer {
    private final TransactionDataCborSerializer dataSerializer;

    /**
     * Constructs a new TransactionCborSerializer instance.
     *
     * @param predicateFactory A factory for creating predicates used in transaction data deserialization.
     */
    public TransactionCborSerializer(IPredicateFactory predicateFactory) {
        this.dataSerializer = new TransactionDataCborSerializer(predicateFactory);
    }

    /**
     * Serializes a Transaction object containing TransactionData into a CBOR-encoded byte array.
     *
     * @param transaction The transaction to serialize.
     * @return The CBOR-encoded representation of the transaction.
     */
    public static byte[] serialize(Transaction<TransactionData> transaction) {
        return CborEncoder.encodeArray(
            TransactionDataCborSerializer.serialize(transaction.getData()),
            transaction.getInclusionProof().toCBOR()
        );
    }

    /**
     * Deserializes a CBOR-encoded byte array into a Transaction object containing TransactionData.
     *
     * @param tokenId The ID of the token associated with the transaction.
     * @param tokenType The type of the token associated with the transaction.
     * @param bytes The CBOR-encoded data to deserialize.
     * @return A CompletableFuture that resolves to the deserialized transaction.
     */
    public CompletableFuture<Transaction> deserialize(
        TokenId tokenId,
        TokenType tokenType,
        byte[] bytes
    ) {
        try {
            CustomCborDecoder.DecodeResult result = CustomCborDecoder.decode(bytes, 0);
            
            if (!(result.value instanceof List)) {
                return CompletableFuture.failedFuture(new RuntimeException("Expected array for Transaction"));
            }
            
            List<?> data = (List<?>) result.value;
            if (data.size() < 2) {
                return CompletableFuture.failedFuture(new RuntimeException("Invalid Transaction array size"));
            }
            
            // Deserialize transaction data
            return dataSerializer.deserialize(tokenId, tokenType, (byte[]) data.get(0))
                .thenApply(transactionData -> {
                    // Deserialize inclusion proof
                    InclusionProof inclusionProof = InclusionProof.fromCBOR((byte[]) data.get(1));
                    
                    return new Transaction(transactionData, inclusionProof);
                });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}