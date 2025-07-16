
package com.unicity.sdk.transaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.util.HexConverter;
import com.unicity.sdk.token.NameTagToken;
import com.unicity.sdk.token.TokenState;

import java.util.Arrays;
import java.util.List;

/**
 * Transaction data for token state transitions
 */
public class TransferTransactionData implements TransactionData<TokenState> {
    private final TokenState sourceState;
    private final String recipient;
    private final byte[] salt;
    private final DataHash data;
    private final byte[] message;
    private final List<NameTagToken> nametagTokens;
    private final DataHash hash;

    private TransferTransactionData(
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

    public static TransferTransactionData create(
            TokenState sourceState,
            String recipient,
            byte[] salt,
            DataHash data,
            byte[] message) {
        return create(sourceState, recipient, salt, data, message, null);
    }

    public static TransferTransactionData create(
            TokenState sourceState,
            String recipient,
            byte[] salt,
            DataHash data,
            byte[] message,
            List<NameTagToken> nametagTokens) {
        
        DataHasher hasher = new DataHasher(HashAlgorithm.SHA256);
//        hasher.update(sourceState.getHash().toCBOR());
        hasher.update(HexConverter.decode(recipient));
        hasher.update(salt);
        if (data != null) {
//            hasher.update(data.toCBOR());
        }
        if (message != null) {
            hasher.update(message);
        }
        // TODO: Add nametag tokens hashing when implemented
        
        return new TransferTransactionData(sourceState, recipient, salt, data, message != null ? message : new byte[0], nametagTokens, hasher.digest());
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
    
    public DataHash getDataHash() {
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
    
    /**
     * Deserialize TransactionData from JSON.
     * @param jsonNode JSON node containing transaction data
     * @return TransactionData instance
     */
    public static TransferTransactionData fromJSON(JsonNode jsonNode) throws Exception {
        // Deserialize source state
        JsonNode sourceStateNode = jsonNode.get("sourceState");
        TokenState sourceState = TokenState.fromJSON(sourceStateNode);
        
        // Get recipient
        String recipient = jsonNode.get("recipient").asText();
        
        // Get salt
        String saltHex = jsonNode.get("salt").asText();
        byte[] salt = HexConverter.decode(saltHex);
        
        // Get data hash
        JsonNode dataNode = jsonNode.get("data");
        DataHash data = null;
//        DataHash data = DataHash.fromJSON(dataNode.asText());
        
        // Get message
        String messageHex = jsonNode.get("message").asText();
        byte[] message = HexConverter.decode(messageHex);
        
        // TODO: Handle nametag tokens when implemented
        
        return create(sourceState, recipient, salt, data, message);
    }
}
