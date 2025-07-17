package com.unicity.sdk.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.BlockHeightResponse;
import com.unicity.sdk.api.InclusionProofRequest;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.api.SubmitCommitmentRequest;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.jsonrpc.JsonRpcError;
import com.unicity.sdk.jsonrpc.JsonRpcRequest;
import com.unicity.sdk.jsonrpc.JsonRpcResponse;
import com.unicity.sdk.serializer.cbor.api.AuthenticatorCborSerializer;
import com.unicity.sdk.serializer.cbor.hash.DataHashCbor;
import com.unicity.sdk.serializer.cbor.token.TokenTypeCbor;
import com.unicity.sdk.serializer.json.api.AuthenticatorJson;
import com.unicity.sdk.serializer.json.api.BlockHeightResponseJson;
import com.unicity.sdk.serializer.json.api.InclusionProofRequestJson;
import com.unicity.sdk.serializer.json.api.RequestIdJson;
import com.unicity.sdk.serializer.json.api.SubmitCommitmentRequestJson;
import com.unicity.sdk.serializer.json.hash.DataHashJson;
import com.unicity.sdk.serializer.json.jsonrpc.JsonRpcErrorJson;
import com.unicity.sdk.serializer.json.jsonrpc.JsonRpcRequestJson;
import com.unicity.sdk.serializer.json.jsonrpc.JsonRpcRequestJson.Serializer;
import com.unicity.sdk.serializer.json.jsonrpc.JsonRpcResponseJson;
import com.unicity.sdk.serializer.json.smt.MerkleTreePathJson;
import com.unicity.sdk.serializer.json.smt.MerkleTreePathStepBranchJson;
import com.unicity.sdk.serializer.json.smt.MerkleTreePathStepJson;
import com.unicity.sdk.serializer.json.transaction.InclusionProofJson;
import com.unicity.sdk.smt.path.MerkleTreePath;
import com.unicity.sdk.smt.path.MerkleTreePathStep;
import com.unicity.sdk.smt.path.MerkleTreePathStepBranch;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.InclusionProof;

public class UnicityObjectMapper {

  public static final ObjectMapper CBOR = createCborObjectMapper();
  public static final ObjectMapper JSON = createJsonObjectMapper();

  private static ObjectMapper createCborObjectMapper() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(DataHash.class, new DataHashCbor.Serializer());
    module.addDeserializer(DataHash.class, new DataHashCbor.Deserializer());

    module.addSerializer(Authenticator.class, new AuthenticatorCborSerializer.Serializer());
    module.addDeserializer(Authenticator.class, new AuthenticatorCborSerializer.Deserializer());

    module.addSerializer(TokenType.class, new TokenTypeCbor.Serializer());
    module.addDeserializer(TokenType.class, new TokenTypeCbor.Deserializer());

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

    module.addSerializer(MerkleTreePathStepBranch.class,
        new MerkleTreePathStepBranchJson.Serializer());
    module.addDeserializer(MerkleTreePathStepBranch.class,
        new MerkleTreePathStepBranchJson.Deserializer());

    module.addSerializer(Authenticator.class, new AuthenticatorJson.Serializer());
    module.addDeserializer(Authenticator.class, new AuthenticatorJson.Deserializer());

    module.addSerializer(RequestId.class, new RequestIdJson.Serializer());
    module.addDeserializer(RequestId.class, new RequestIdJson.Deserializer());

    module.addSerializer(JsonRpcRequest.class, new JsonRpcRequestJson.Serializer());

    module.addDeserializer(JsonRpcResponse.class, new JsonRpcResponseJson.Deserializer());
    module.addDeserializer(JsonRpcError.class, new JsonRpcErrorJson.Deserializer());

    module.addSerializer(SubmitCommitmentRequest.class,
        new SubmitCommitmentRequestJson.Serializer());
    module.addSerializer(InclusionProofRequest.class, new InclusionProofRequestJson.Serializer());

    module.addDeserializer(BlockHeightResponse.class, new BlockHeightResponseJson.Deserializer());

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(module);
    return objectMapper;
  }
}
