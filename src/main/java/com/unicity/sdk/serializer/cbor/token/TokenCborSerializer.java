package com.unicity.sdk.serializer.cbor.token;

import com.unicity.sdk.predicate.IPredicateFactory;
import com.unicity.sdk.serializer.cbor.transaction.MintTransactionCborSerializer;
import com.unicity.sdk.serializer.cbor.transaction.TransactionCborSerializer;
import com.unicity.sdk.serializer.token.ITokenSerializer;
import com.unicity.sdk.shared.cbor.CborDecoder;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.cbor.CustomCborDecoder;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransactionData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A serializer for {@link Token} objects using CBOR encoding.
 * Handles serialization and deserialization of tokens, including their transactions and state.
 */
public class TokenCborSerializer implements ITokenSerializer {
    private final MintTransactionCborSerializer mintTransactionSerializer;
    private final TransactionCborSerializer transactionSerializer;
    private final TokenStateCborSerializer stateSerializer;
    private final IPredicateFactory predicateFactory;

    /**
     * Constructs a new TokenCborSerializer instance.
     *
     * @param predicateFactory A factory for creating predicates used in token serialization.
     */
    public TokenCborSerializer(IPredicateFactory predicateFactory) {
        this.predicateFactory = predicateFactory;
        this.mintTransactionSerializer = new MintTransactionCborSerializer(this);
        this.transactionSerializer = new TransactionCborSerializer(predicateFactory);
        this.stateSerializer = new TokenStateCborSerializer(predicateFactory);
    }

    /**
     * Serializes a Token object into a CBOR-encoded byte array.
     *
     * @param token The token to serialize.
     * @return The CBOR-encoded representation of the token.
     */
    public static byte[] serialize(Token<?> token) {
        // Serialize transactions
        List<byte[]> transactionBytes = new ArrayList<>();
        for (Transaction<?> transaction : token.getTransactions()) {
            transactionBytes.add(TransactionCborSerializer.serialize((Transaction<TransactionData>) transaction));
        }

        // Serialize nametag tokens
        List<byte[]> nametagTokenBytes = new ArrayList<>();
        for (Token<?> nametagToken : token.getNametagTokens()) {
            nametagTokenBytes.add(nametagToken.toCBOR());
        }

        return CborEncoder.encodeArray(
            CborEncoder.encodeTextString(token.getVersion()),
            MintTransactionCborSerializer.serialize((Transaction) token.getGenesis()),
            CborEncoder.encodeArray(transactionBytes.toArray(new byte[0][])),
            TokenStateCborSerializer.serialize(token.getState()),
            CborEncoder.encodeArray(nametagTokenBytes.toArray(new byte[0][]))
        );
    }

    /**
     * Deserializes a CBOR-encoded byte array into a Token object.
     *
     * @param bytes The CBOR-encoded data to deserialize.
     * @return A CompletableFuture that resolves to the deserialized Token object.
     * @throws RuntimeException If the token version does not match the expected version.
     */
    @Override
    public CompletableFuture<Token> deserialize(byte[] bytes) {
        try {
            CustomCborDecoder.DecodeResult result = CustomCborDecoder.decode(bytes, 0);
            
            if (!(result.value instanceof List)) {
                return CompletableFuture.failedFuture(new RuntimeException("Expected array for Token"));
            }
            
            List<?> data = (List<?>) result.value;
            if (data.size() < 5) {
                return CompletableFuture.failedFuture(new RuntimeException("Invalid Token array size"));
            }
            
            // Read version
            String tokenVersion = (String) data.get(0);
            if (!Token.TOKEN_VERSION.equals(tokenVersion)) {
                return CompletableFuture.failedFuture(
                    new RuntimeException("Cannot parse token. Version mismatch: " + tokenVersion + " !== " + Token.TOKEN_VERSION)
                );
            }
            
            // Deserialize genesis transaction
            return mintTransactionSerializer.deserialize((byte[]) data.get(1))
                .thenCompose(mintTransaction -> {
                    // Deserialize transactions
                    List<CompletableFuture<Transaction>> transactionFutures = new ArrayList<>();
                    List<?> transactionArray = (List<?>) data.get(2);
                    
                    for (Object txData : transactionArray) {
                        transactionFutures.add(
                            transactionSerializer.deserialize(
                                mintTransaction.getData().getTokenId(),
                                mintTransaction.getData().getTokenType(),
                                (byte[]) txData
                            )
                        );
                    }
                    
                    return CompletableFuture.allOf(transactionFutures.toArray(new CompletableFuture[0]))
                        .thenCompose(v -> {
                            List<Transaction> transactions = new ArrayList<>();
                            for (CompletableFuture<Transaction> future : transactionFutures) {
                                transactions.add(future.join());
                            }
                            
                            // Deserialize state
                            return stateSerializer.deserialize(
                                mintTransaction.getData().getTokenId(),
                                mintTransaction.getData().getTokenType(),
                                (byte[]) data.get(3)
                            ).thenApply(state -> {
                                // TODO: Add nametag tokens deserialization
                                List<Token> nametagTokens = new ArrayList<>();
                                
                                return new Token(
                                    state,
                                    mintTransaction,
                                    transactions,
                                    nametagTokens,
                                    tokenVersion
                                );
                            });
                        });
                });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}