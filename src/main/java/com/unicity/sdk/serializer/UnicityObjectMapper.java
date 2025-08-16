package com.unicity.sdk.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.unicity.sdk.address.Address;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.BlockHeightResponse;
import com.unicity.sdk.api.InclusionProofRequest;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.api.SubmitCommitmentRequest;
import com.unicity.sdk.api.SubmitCommitmentResponse;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.jsonrpc.JsonRpcError;
import com.unicity.sdk.jsonrpc.JsonRpcRequest;
import com.unicity.sdk.jsonrpc.JsonRpcResponse;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePath;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStep;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStepBranch;
import com.unicity.sdk.predicate.Predicate;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.predicate.UnmaskedPredicate;
import com.unicity.sdk.serializer.cbor.api.AuthenticatorCbor;
import com.unicity.sdk.serializer.cbor.hash.DataHashCbor;
import com.unicity.sdk.serializer.cbor.token.TokenTypeCbor;
import com.unicity.sdk.serializer.json.address.AddressJson;
import com.unicity.sdk.serializer.json.api.AuthenticatorJson;
import com.unicity.sdk.serializer.json.api.BlockHeightResponseJson;
import com.unicity.sdk.serializer.json.api.InclusionProofRequestJson;
import com.unicity.sdk.serializer.json.api.RequestIdJson;
import com.unicity.sdk.serializer.json.api.SubmitCommitmentRequestJson;
import com.unicity.sdk.serializer.json.api.SubmitCommitmentResponseJson;
import com.unicity.sdk.serializer.json.hash.DataHashJson;
import com.unicity.sdk.serializer.json.jsonrpc.JsonRpcErrorJson;
import com.unicity.sdk.serializer.json.jsonrpc.JsonRpcRequestJson;
import com.unicity.sdk.serializer.json.jsonrpc.JsonRpcResponseJson;
import com.unicity.sdk.serializer.json.mtree.plain.MerkleTreePathJson;
import com.unicity.sdk.serializer.json.mtree.plain.MerkleTreePathStepBranchJson;
import com.unicity.sdk.serializer.json.mtree.plain.MerkleTreePathStepJson;
import com.unicity.sdk.serializer.json.predicate.MaskedPredicateJson;
import com.unicity.sdk.serializer.json.predicate.PredicateJson;
import com.unicity.sdk.serializer.json.predicate.UnmaskedPredicateJson;
import com.unicity.sdk.serializer.json.token.TokenCoinDataJson;
import com.unicity.sdk.serializer.json.token.TokenIdJson;
import com.unicity.sdk.serializer.json.token.TokenJson;
import com.unicity.sdk.serializer.json.token.TokenStateJson;
import com.unicity.sdk.serializer.json.token.TokenTypeJson;
import com.unicity.sdk.serializer.json.transaction.CommitmentJson;
import com.unicity.sdk.serializer.json.transaction.InclusionProofJson;
import com.unicity.sdk.serializer.json.transaction.MintTransactionDataJson;
import com.unicity.sdk.serializer.json.transaction.TransactionJson;
import com.unicity.sdk.serializer.json.transaction.TransferTransactionDataJson;
import com.unicity.sdk.serializer.json.util.ByteArrayHexJson;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferTransactionData;

public class UnicityObjectMapper {

  public static final ObjectMapper CBOR = createCborObjectMapper();
  public static final ObjectMapper JSON = createJsonObjectMapper();

  private static ObjectMapper createCborObjectMapper() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(DataHash.class, new DataHashCbor.Serializer());
    module.addDeserializer(DataHash.class, new DataHashCbor.Deserializer());

    module.addSerializer(Authenticator.class, new AuthenticatorCbor.Serializer());
    module.addDeserializer(Authenticator.class, new AuthenticatorCbor.Deserializer());

    module.addSerializer(TokenType.class, new TokenTypeCbor.Serializer());
    module.addDeserializer(TokenType.class, new TokenTypeCbor.Deserializer());

    ObjectMapper objectMapper = new CBORMapper();
    objectMapper.registerModule(module);
    return objectMapper;
  }

  private static ObjectMapper createJsonObjectMapper() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(byte[].class, new ByteArrayHexJson.Serializer());
    module.addDeserializer(byte[].class, new ByteArrayHexJson.Deserializer());

    module.addSerializer(Address.class, new AddressJson.Serializer());
    module.addDeserializer(Address.class, new AddressJson.Deserializer());

    module.addSerializer(TokenId.class, new TokenIdJson.Serializer());
    module.addDeserializer(TokenId.class, new TokenIdJson.Deserializer());

    module.addSerializer(TokenType.class, new TokenTypeJson.Serializer());
    module.addDeserializer(TokenType.class, new TokenTypeJson.Deserializer());

    module.addSerializer(TokenCoinData.class, new TokenCoinDataJson.Serializer());
    module.addDeserializer(TokenCoinData.class, new TokenCoinDataJson.Deserializer());

    module.addSerializer(DataHash.class, new DataHashJson.Serializer());
    module.addDeserializer(DataHash.class, new DataHashJson.Deserializer());

    module.addSerializer(InclusionProof.class, new InclusionProofJson.Serializer());
    module.addDeserializer(InclusionProof.class, new InclusionProofJson.Deserializer());

    module.addSerializer(SparseMerkleTreePath.class, new MerkleTreePathJson.Serializer());
    module.addDeserializer(SparseMerkleTreePath.class, new MerkleTreePathJson.Deserializer());

    module.addSerializer(SparseMerkleTreePathStep.class, new MerkleTreePathStepJson.Serializer());
    module.addDeserializer(SparseMerkleTreePathStep.class, new MerkleTreePathStepJson.Deserializer());

    module.addSerializer(SparseMerkleTreePathStepBranch.class,
        new MerkleTreePathStepBranchJson.Serializer());
    module.addDeserializer(SparseMerkleTreePathStepBranch.class,
        new MerkleTreePathStepBranchJson.Deserializer());

    module.addSerializer(Authenticator.class, new AuthenticatorJson.Serializer());
    module.addDeserializer(Authenticator.class, new AuthenticatorJson.Deserializer());

    module.addSerializer(RequestId.class, new RequestIdJson.Serializer());
    module.addDeserializer(RequestId.class, new RequestIdJson.Deserializer());

    module.addSerializer(Commitment.class, new CommitmentJson.Serializer());
    module.addDeserializer(Commitment.class, new CommitmentJson.Deserializer());

    module.addSerializer(Token.class, new TokenJson.Serializer());
    module.addDeserializer(Token.class, new TokenJson.Deserializer());

    module.addSerializer(TokenState.class, new TokenStateJson.Serializer());
    module.addDeserializer(TokenState.class, new TokenStateJson.Deserializer());

    module.addDeserializer(Predicate.class, new PredicateJson.Deserializer());
    module.addSerializer(MaskedPredicate.class, new MaskedPredicateJson.Serializer());
    module.addDeserializer(MaskedPredicate.class, new MaskedPredicateJson.Deserializer());
    module.addSerializer(UnmaskedPredicate.class, new UnmaskedPredicateJson.Serializer());
    module.addDeserializer(UnmaskedPredicate.class, new UnmaskedPredicateJson.Deserializer());

    module.addSerializer(Transaction.class, new TransactionJson.Serializer());
    module.addDeserializer(Transaction.class, new TransactionJson.Deserializer());

    module.addSerializer(MintTransactionData.class, new MintTransactionDataJson.Serializer());
    module.addDeserializer(MintTransactionData.class, new MintTransactionDataJson.Deserializer());

    module.addSerializer(TransferTransactionData.class,
        new TransferTransactionDataJson.Serializer());
    module.addDeserializer(TransferTransactionData.class,
        new TransferTransactionDataJson.Deserializer());

    module.addSerializer(JsonRpcRequest.class, new JsonRpcRequestJson.Serializer());
    module.addSerializer(SubmitCommitmentRequest.class,
        new SubmitCommitmentRequestJson.Serializer());
    module.addSerializer(InclusionProofRequest.class, new InclusionProofRequestJson.Serializer());

    module.addDeserializer(JsonRpcResponse.class, new JsonRpcResponseJson.Deserializer());
    module.addDeserializer(JsonRpcError.class, new JsonRpcErrorJson.Deserializer());
    module.addDeserializer(BlockHeightResponse.class, new BlockHeightResponseJson.Deserializer());
    module.addDeserializer(SubmitCommitmentResponse.class,
        new SubmitCommitmentResponseJson.Deserializer());

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new Jdk8Module());
    objectMapper.registerModule(module);
    return objectMapper;
  }
}
