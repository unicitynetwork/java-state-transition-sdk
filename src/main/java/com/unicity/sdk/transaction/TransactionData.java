
package com.unicity.sdk.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.JavaDataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.util.HexConverter;
import com.unicity.sdk.token.NameTagToken;
import com.unicity.sdk.token.TokenState;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Transaction data for token state transitions
 */
public class TransactionData implements ISerializable {
    private final TokenState sourceState;
    private final String recipient;
    private final byte[] salt;
    private final DataHash data;
    private final byte[] message;
    private final List<NameTagToken> nametagTokens;
    private final DataHash hash;

    private TransactionData(
            TokenState sourceState,
            String recipient,
            byte[] salt,
            DataHash data,
            byte[] message,
            List<NameTagToken> nametagTokens,
            DataHash hash) {
        this.sourceState = sourceState;
        this.recipient = recipient;
        this.salt = Arrays.copyOf(salt, salt.length);
        this.data = data;
        this.message = Arrays.copyOf(message, message.length);
        this.nametagTokens = nametagTokens;
        this.hash = hash;
    }

    public static CompletableFuture<TransactionData> create(
            TokenState sourceState,
            String recipient,
            byte[] salt,
            DataHash data,
            byte[] message) {
        return create(sourceState, recipient, salt, data, message, null);
    }

    public static CompletableFuture<TransactionData> create(
            TokenState sourceState,
            String recipient,
            byte[] salt,
            DataHash data,
            byte[] message,
            List<NameTagToken> nametagTokens) {
        
        JavaDataHasher hasher = new JavaDataHasher(HashAlgorithm.SHA256);
        hasher.update(sourceState.getHash().toCBOR());
        hasher.update(HexConverter.decode(recipient));
        hasher.update(salt);
        hasher.update(data.toCBOR());
        if (message != null) {
            hasher.update(message);
        }
        // TODO: Add nametag tokens hashing when implemented
        
        return hasher.digest().thenApply(hash ->
            new TransactionData(sourceState, recipient, salt, data, 
                message != null ? message : new byte[0], nametagTokens, hash)
        );
    }

    public TokenState getSourceState() {
        return sourceState;
    }

    public String getRecipient() {
        return recipient;
    }

    public byte[] getSalt() {
        return Arrays.copyOf(salt, salt.length);
    }

    public DataHash getData() {
        return data;
    }

    public byte[] getMessage() {
        return Arrays.copyOf(message, message.length);
    }

    public List<NameTagToken> getNametagTokens() {
        return nametagTokens;
    }

    public DataHash getHash() {
        return hash;
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        
        root.set("sourceState", mapper.valueToTree(sourceState.toJSON()));
        root.put("recipient", recipient);
        root.put("salt", HexConverter.encode(salt));
        root.set("data", mapper.valueToTree(data.toJSON()));
        root.put("message", HexConverter.encode(message));
        // TODO: Add nametag tokens when implemented
        
        return root;
    }

    @Override
    public byte[] toCBOR() {
        return CborEncoder.encodeArray(
            sourceState.toCBOR(),
            CborEncoder.encodeTextString(recipient),
            CborEncoder.encodeByteString(salt),
            data.toCBOR(),
            CborEncoder.encodeByteString(message)
            // TODO: Add nametag tokens when implemented
        );
    }
}
