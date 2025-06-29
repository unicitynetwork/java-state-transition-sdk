package com.unicity.sdk.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.shared.cbor.CborEncoder;

/**
 * Result returned when creating an offline transaction commitment.
 * This commitment can be serialized and transferred offline to another party.
 */
public class OfflineCommitment implements ISerializable {
    private final RequestId requestId;
    private final TransactionData transactionData;
    private final Authenticator authenticator;

    /**
     * @param requestId       Request identifier used for submission
     * @param transactionData Submitted transaction data
     * @param authenticator   Signature over the payload
     */
    public OfflineCommitment(RequestId requestId, TransactionData transactionData, Authenticator authenticator) {
        if (requestId == null) {
            throw new IllegalArgumentException("RequestId cannot be null");
        }
        if (transactionData == null) {
            throw new IllegalArgumentException("TransactionData cannot be null");
        }
        if (authenticator == null) {
            throw new IllegalArgumentException("Authenticator cannot be null");
        }
        this.requestId = requestId;
        this.transactionData = transactionData;
        this.authenticator = authenticator;
    }

    public RequestId getRequestId() {
        return requestId;
    }

    public TransactionData getTransactionData() {
        return transactionData;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        
        root.set("requestId", mapper.valueToTree(requestId.toJSON()));
        root.set("transactionData", mapper.valueToTree(transactionData.toJSON()));
        root.set("authenticator", mapper.valueToTree(authenticator.toJSON()));
        
        return root;
    }

    @Override
    public byte[] toCBOR() {
        return CborEncoder.encodeArray(
            requestId.toCBOR(),
            transactionData.toCBOR(),
            authenticator.toCBOR()
        );
    }
}
