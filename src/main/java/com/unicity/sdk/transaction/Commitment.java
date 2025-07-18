
package com.unicity.sdk.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.shared.cbor.CborEncoder;

/**
 * Commitment representing a submitted transaction
 */
public class Commitment<T extends ISerializable> implements ISerializable {
    private final RequestId requestId;
    private final T transactionData;
    private final Authenticator authenticator;

    public Commitment(RequestId requestId, T transactionData, Authenticator authenticator) {
        this.requestId = requestId;
        this.transactionData = transactionData;
        this.authenticator = authenticator;
    }

    public RequestId getRequestId() {
        return requestId;
    }

    public T getTransactionData() {
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
