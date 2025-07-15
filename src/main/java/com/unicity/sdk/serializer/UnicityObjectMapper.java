package com.unicity.sdk.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.serializer.cbor.api.AuthenticatorCborSerializer;
import com.unicity.sdk.serializer.cbor.hash.DataHashCborSerializer;
import com.unicity.sdk.serializer.json.hash.DataHashJson;
import com.unicity.sdk.serializer.json.smt.MerkleTreePathJson;
import com.unicity.sdk.serializer.json.smt.MerkleTreePathStepBranchJson;
import com.unicity.sdk.serializer.json.smt.MerkleTreePathStepJson;
import com.unicity.sdk.serializer.json.transaction.InclusionProofJson;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.smt.path.MerkleTreePath;
import com.unicity.sdk.shared.smt.path.MerkleTreePathStep;
import com.unicity.sdk.shared.smt.path.MerkleTreePathStepBranch;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.serializer.json.api.AuthenticatorJson;

public class UnicityObjectMapper {
    public static final ObjectMapper CBOR = createCborObjectMapper();
    public static final ObjectMapper JSON = createJsonObjectMapper();

    private static ObjectMapper createCborObjectMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataHash.class, new DataHashCborSerializer.Serializer());
        module.addDeserializer(DataHash.class, new DataHashCborSerializer.Deserializer());

        module.addSerializer(Authenticator.class, new AuthenticatorCborSerializer.Serializer());
        module.addDeserializer(Authenticator.class, new AuthenticatorCborSerializer.Deserializer());

        ObjectMapper objectMapper = new CBORMapper();
        objectMapper.registerModule(module);
        return objectMapper;
    }

    private static ObjectMapper createJsonObjectMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataHash.class, new DataHashJson.Serializer());
        module.addDeserializer(DataHash.class, new DataHashJson.Deserializer());

        module.addSerializer(InclusionProof.class, new InclusionProofJson.Serializer());
        module.addDeserializer(InclusionProof.class, new InclusionProofJson.Deserializer());

        module.addSerializer(MerkleTreePath.class, new MerkleTreePathJson.Serializer());
        module.addDeserializer(MerkleTreePath.class, new MerkleTreePathJson.Deserializer());

        module.addSerializer(MerkleTreePathStep.class, new MerkleTreePathStepJson.Serializer());
        module.addDeserializer(MerkleTreePathStep.class, new MerkleTreePathStepJson.Deserializer());

        module.addSerializer(MerkleTreePathStepBranch.class, new MerkleTreePathStepBranchJson.Serializer());
        module.addDeserializer(MerkleTreePathStepBranch.class, new MerkleTreePathStepBranchJson.Deserializer());

        module.addSerializer(Authenticator.class, new AuthenticatorJson.Serializer());
        module.addDeserializer(Authenticator.class, new AuthenticatorJson.Deserializer());


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
