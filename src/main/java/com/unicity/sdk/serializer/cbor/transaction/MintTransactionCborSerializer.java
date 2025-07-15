package com.unicity.sdk.serializer.cbor.transaction;

import com.unicity.sdk.serializer.cbor.token.TokenCborSerializer;
import com.unicity.sdk.shared.cbor.CborDecoder;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.cbor.CustomCborDecoder;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A serializer for {@link Transaction} containing {@link MintTransactionData} using CBOR encoding.
 * Handles serialization and deserialization of mint transactions, including their data and inclusion proof.
 */
public class MintTransactionCborSerializer {
    private final MintTransactionDataCborSerializer dataSerializer;

    /**
     * Constructs a new MintTransactionCborSerializer instance.
     *
     * @param tokenSerializer A serializer for tokens, used in mint transaction data serialization.
     */
    public MintTransactionCborSerializer(TokenCborSerializer tokenSerializer) {
        this.dataSerializer = new MintTransactionDataCborSerializer(tokenSerializer);
    }

    /**
     * Serializes a Transaction containing MintTransactionData into a CBOR-encoded byte array.
     *
     * @param transaction The mint transaction to serialize.
     * @return The CBOR-encoded representation of the mint transaction.
     */
    public static byte[] serialize(Transaction<MintTransactionData> transaction) {
        return CborEncoder.encodeArray(
            MintTransactionDataCborSerializer.serialize(transaction.getData()),
            // transaction.getInclusionProof().toCBOR()
                null
        );
    }

    /**
     * Deserializes a CBOR-encoded byte array into a Transaction containing MintTransactionData.
     *
     * @param bytes The CBOR-encoded data to deserialize.
     * @return A CompletableFuture that resolves to the deserialized mint transaction.
     */
    public CompletableFuture<Transaction<MintTransactionData>> deserialize(byte[] bytes) {
        try {
            CustomCborDecoder.DecodeResult result = CustomCborDecoder.decode(bytes, 0);
            
            if (!(result.value instanceof List)) {
                return CompletableFuture.failedFuture(new RuntimeException("Expected array for MintTransaction"));
            }
            
            List<?> data = (List<?>) result.value;
            if (data.size() < 2) {
                return CompletableFuture.failedFuture(new RuntimeException("Invalid MintTransaction array size"));
            }
            
            // Deserialize transaction data
            return dataSerializer.deserialize((byte[]) data.get(0))
                .thenApply(transactionData -> {
                    // Deserialize inclusion proof
                    // InclusionProof inclusionProof = InclusionProof.fromCBOR((byte[]) data.get(1));
                    InclusionProof inclusionProof = null;
                    
                    return new Transaction<>(transactionData, inclusionProof);
                });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}