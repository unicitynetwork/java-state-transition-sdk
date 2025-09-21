package org.unicitylabs.sdk.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.BlockHeightResponse;
import org.unicitylabs.sdk.api.InclusionProofRequest;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.api.SubmitCommitmentRequest;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.bft.InputRecord;
import org.unicitylabs.sdk.bft.ShardTreeCertificate;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.bft.UnicitySeal;
import org.unicitylabs.sdk.bft.UnicityTreeCertificate;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.jsonrpc.JsonRpcError;
import org.unicitylabs.sdk.jsonrpc.JsonRpcRequest;
import org.unicitylabs.sdk.jsonrpc.JsonRpcResponse;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePathStep;
import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTreePath;
import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTreePathStep;
import org.unicitylabs.sdk.predicate.SerializablePredicate;
import org.unicitylabs.sdk.predicate.embedded.BurnPredicate;
import org.unicitylabs.sdk.predicate.embedded.DefaultPredicate;
import org.unicitylabs.sdk.predicate.embedded.MaskedPredicate;
import org.unicitylabs.sdk.predicate.embedded.UnmaskedPredicate;
import org.unicitylabs.sdk.serializer.cbor.address.AddressCbor;
import org.unicitylabs.sdk.serializer.cbor.api.AuthenticatorCbor;
import org.unicitylabs.sdk.serializer.cbor.bft.InputRecordCbor;
import org.unicitylabs.sdk.serializer.cbor.bft.ShardTreeCertificateCbor;
import org.unicitylabs.sdk.serializer.cbor.bft.UnicityCertificateCbor;
import org.unicitylabs.sdk.serializer.cbor.bft.UnicitySealCbor;
import org.unicitylabs.sdk.serializer.cbor.bft.UnicityTreeCertificateCbor;
import org.unicitylabs.sdk.serializer.cbor.bft.UnicityTreeCertificateHashStepCbor;
import org.unicitylabs.sdk.serializer.cbor.hash.DataHashCbor;
import org.unicitylabs.sdk.serializer.cbor.mtree.plain.SparseMerkleTreePathCbor;
import org.unicitylabs.sdk.serializer.cbor.mtree.plain.SparseMerkleTreePathStepBranchCbor;
import org.unicitylabs.sdk.serializer.cbor.mtree.plain.SparseMerkleTreePathStepCbor;
import org.unicitylabs.sdk.serializer.cbor.mtree.sum.SparseMerkleSumTreePathCbor;
import org.unicitylabs.sdk.serializer.cbor.mtree.sum.SparseMerkleSumTreePathStepBranchCbor;
import org.unicitylabs.sdk.serializer.cbor.mtree.sum.SparseMerkleSumTreePathStepCbor;
import org.unicitylabs.sdk.serializer.cbor.predicate.BurnPredicateCbor;
import org.unicitylabs.sdk.serializer.cbor.predicate.DefaultPredicateCbor;
import org.unicitylabs.sdk.serializer.cbor.predicate.MaskedPredicateCbor;
import org.unicitylabs.sdk.serializer.cbor.predicate.SerializablePredicateCbor;
import org.unicitylabs.sdk.serializer.cbor.predicate.UnmaskedPredicateCbor;
import org.unicitylabs.sdk.serializer.cbor.token.TokenCbor;
import org.unicitylabs.sdk.serializer.cbor.token.TokenIdCbor;
import org.unicitylabs.sdk.serializer.cbor.token.TokenStateCbor;
import org.unicitylabs.sdk.serializer.cbor.token.TokenTypeCbor;
import org.unicitylabs.sdk.serializer.cbor.token.fungible.TokenCoinDataCbor;
import org.unicitylabs.sdk.serializer.cbor.transaction.InclusionProofCbor;
import org.unicitylabs.sdk.serializer.cbor.transaction.MintTransactionDataCbor;
import org.unicitylabs.sdk.serializer.cbor.transaction.MintTransactionReasonCbor;
import org.unicitylabs.sdk.serializer.cbor.transaction.TransactionCbor;
import org.unicitylabs.sdk.serializer.cbor.transaction.TransferTransactionDataCbor;
import org.unicitylabs.sdk.serializer.cbor.transaction.split.SplitMintReasonCbor;
import org.unicitylabs.sdk.serializer.cbor.transaction.split.SplitMintReasonProofCbor;
import org.unicitylabs.sdk.serializer.json.address.AddressJson;
import org.unicitylabs.sdk.serializer.json.api.AuthenticatorJson;
import org.unicitylabs.sdk.serializer.json.api.BlockHeightResponseJson;
import org.unicitylabs.sdk.serializer.json.api.InclusionProofRequestJson;
import org.unicitylabs.sdk.serializer.json.api.RequestIdJson;
import org.unicitylabs.sdk.serializer.json.api.SubmitCommitmentRequestJson;
import org.unicitylabs.sdk.serializer.json.api.SubmitCommitmentResponseJson;
import org.unicitylabs.sdk.serializer.json.hash.DataHashJson;
import org.unicitylabs.sdk.serializer.json.jsonrpc.JsonRpcErrorJson;
import org.unicitylabs.sdk.serializer.json.jsonrpc.JsonRpcRequestJson;
import org.unicitylabs.sdk.serializer.json.jsonrpc.JsonRpcResponseJson;
import org.unicitylabs.sdk.serializer.json.mtree.plain.SparseMerkleTreePathJson;
import org.unicitylabs.sdk.serializer.json.mtree.plain.SparseMerkleTreePathStepBranchJson;
import org.unicitylabs.sdk.serializer.json.mtree.plain.SparseMerkleTreePathStepJson;
import org.unicitylabs.sdk.serializer.json.mtree.sum.SparseMerkleSumTreePathJson;
import org.unicitylabs.sdk.serializer.json.mtree.sum.SparseMerkleSumTreePathStepBranchJson;
import org.unicitylabs.sdk.serializer.json.mtree.sum.SparseMerkleSumTreePathStepJson;
import org.unicitylabs.sdk.serializer.json.predicate.SerializablePredicateJson;
import org.unicitylabs.sdk.serializer.json.token.TokenIdJson;
import org.unicitylabs.sdk.serializer.json.token.TokenJson;
import org.unicitylabs.sdk.serializer.json.token.TokenStateJson;
import org.unicitylabs.sdk.serializer.json.token.TokenTypeJson;
import org.unicitylabs.sdk.serializer.json.token.fungible.TokenCoinDataJson;
import org.unicitylabs.sdk.serializer.json.transaction.CommitmentJson;
import org.unicitylabs.sdk.serializer.json.transaction.InclusionProofJson;
import org.unicitylabs.sdk.serializer.json.transaction.MintCommitmentJson;
import org.unicitylabs.sdk.serializer.json.transaction.MintTransactionDataJson;
import org.unicitylabs.sdk.serializer.json.transaction.MintTransactionReasonJson;
import org.unicitylabs.sdk.serializer.json.transaction.TransactionJson;
import org.unicitylabs.sdk.serializer.json.transaction.TransferCommitmentJson;
import org.unicitylabs.sdk.serializer.json.transaction.TransferTransactionDataJson;
import org.unicitylabs.sdk.serializer.json.transaction.split.SplitMintReasonJson;
import org.unicitylabs.sdk.serializer.json.transaction.split.SplitMintReasonProofJson;
import org.unicitylabs.sdk.serializer.json.util.ByteArrayHexJson;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.Commitment;
import org.unicitylabs.sdk.transaction.InclusionProof;
import org.unicitylabs.sdk.transaction.MintCommitment;
import org.unicitylabs.sdk.transaction.MintTransactionData;
import org.unicitylabs.sdk.transaction.MintTransactionReason;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferCommitment;
import org.unicitylabs.sdk.transaction.TransferTransactionData;
import org.unicitylabs.sdk.transaction.split.SplitMintReason;
import org.unicitylabs.sdk.transaction.split.SplitMintReasonProof;

public class UnicityObjectMapper {

  public static final ObjectMapper CBOR = createCborObjectMapper();
  public static final ObjectMapper JSON = createJsonObjectMapper();

  private static ObjectMapper createCborObjectMapper() {
    SimpleModule module = new SimpleModule();

    module.addSerializer(Address.class, new AddressCbor.Serializer());
    module.addDeserializer(Address.class, new AddressCbor.Deserializer());

    module.addSerializer(DataHash.class, new DataHashCbor.Serializer());
    module.addDeserializer(DataHash.class, new DataHashCbor.Deserializer());

    module.addSerializer(SparseMerkleTreePath.class, new SparseMerkleTreePathCbor.Serializer());
    module.addDeserializer(SparseMerkleTreePath.class, new SparseMerkleTreePathCbor.Deserializer());
    module.addSerializer(SparseMerkleTreePathStep.class, new SparseMerkleTreePathStepCbor.Serializer());
    module.addDeserializer(SparseMerkleTreePathStep.class,
        new SparseMerkleTreePathStepCbor.Deserializer());
    module.addSerializer(SparseMerkleTreePathStep.Branch.class,
        new SparseMerkleTreePathStepBranchCbor.Serializer());
    module.addDeserializer(SparseMerkleTreePathStep.Branch.class,
        new SparseMerkleTreePathStepBranchCbor.Deserializer());

    module.addSerializer(SparseMerkleSumTreePath.class,
        new SparseMerkleSumTreePathCbor.Serializer());
    module.addDeserializer(SparseMerkleSumTreePath.class,
        new SparseMerkleSumTreePathCbor.Deserializer());
    module.addSerializer(SparseMerkleSumTreePathStep.class,
        new SparseMerkleSumTreePathStepCbor.Serializer());
    module.addDeserializer(SparseMerkleSumTreePathStep.class,
        new SparseMerkleSumTreePathStepCbor.Deserializer());
    module.addSerializer(SparseMerkleSumTreePathStep.Branch.class,
        new SparseMerkleSumTreePathStepBranchCbor.Serializer());
    module.addDeserializer(SparseMerkleSumTreePathStep.Branch.class,
        new SparseMerkleSumTreePathStepBranchCbor.Deserializer());

    module.addSerializer(Authenticator.class, new AuthenticatorCbor.Serializer());
    module.addDeserializer(Authenticator.class, new AuthenticatorCbor.Deserializer());

    module.addSerializer(InclusionProof.class, new InclusionProofCbor.Serializer());
    module.addDeserializer(InclusionProof.class, new InclusionProofCbor.Deserializer());

    module.addSerializer(Transaction.class, new TransactionCbor.Serializer());
    module.addDeserializer(Transaction.class, new TransactionCbor.Deserializer());

    module.addSerializer(MintTransactionData.class, new MintTransactionDataCbor.Serializer());
    module.addDeserializer(MintTransactionData.class, new MintTransactionDataCbor.Deserializer());

    module.addDeserializer(MintTransactionReason.class,
        new MintTransactionReasonCbor.Deserializer());

    module.addSerializer(TransferTransactionData.class,
        new TransferTransactionDataCbor.Serializer());
    module.addDeserializer(TransferTransactionData.class,
        new TransferTransactionDataCbor.Deserializer());

    module.addSerializer(SplitMintReason.class, new SplitMintReasonCbor.Serializer());
    module.addDeserializer(SplitMintReason.class, new SplitMintReasonCbor.Deserializer());

    module.addSerializer(SplitMintReasonProof.class, new SplitMintReasonProofCbor.Serializer());
    module.addDeserializer(SplitMintReasonProof.class, new SplitMintReasonProofCbor.Deserializer());

    module.addSerializer(TokenId.class, new TokenIdCbor.Serializer());
    module.addDeserializer(TokenId.class, new TokenIdCbor.Deserializer());

    module.addSerializer(TokenType.class, new TokenTypeCbor.Serializer());
    module.addDeserializer(TokenType.class, new TokenTypeCbor.Deserializer());

    module.addSerializer(TokenCoinData.class, new TokenCoinDataCbor.Serializer());
    module.addDeserializer(TokenCoinData.class, new TokenCoinDataCbor.Deserializer());

    module.addSerializer(Token.class, new TokenCbor.Serializer());
    module.addDeserializer(Token.class, new TokenCbor.Deserializer());

    module.addSerializer(TokenState.class, new TokenStateCbor.Serializer());
    module.addDeserializer(TokenState.class, new TokenStateCbor.Deserializer());

    module.addSerializer(SerializablePredicate.class, new SerializablePredicateCbor.Serializer());
    module.addDeserializer(SerializablePredicate.class, new SerializablePredicateCbor.Deserializer());

    module.addSerializer(DefaultPredicate.class, new DefaultPredicateCbor.Serializer());
    module.addSerializer(BurnPredicate.class, new BurnPredicateCbor.Serializer());
    module.addDeserializer(MaskedPredicate.class, new MaskedPredicateCbor.Deserializer());
    module.addDeserializer(UnmaskedPredicate.class, new UnmaskedPredicateCbor.Deserializer());
    module.addDeserializer(BurnPredicate.class, new BurnPredicateCbor.Deserializer());

    // BFT - UnicityCertificate
    module.addSerializer(UnicityCertificate.class, new UnicityCertificateCbor.Serializer());
    module.addDeserializer(UnicityCertificate.class, new UnicityCertificateCbor.Deserializer());
    module.addSerializer(InputRecord.class, new InputRecordCbor.Serializer());
    module.addDeserializer(InputRecord.class, new InputRecordCbor.Deserializer());
    module.addSerializer(ShardTreeCertificate.class, new ShardTreeCertificateCbor.Serializer());
    module.addDeserializer(ShardTreeCertificate.class, new ShardTreeCertificateCbor.Deserializer());
    module.addSerializer(UnicityTreeCertificate.class, new UnicityTreeCertificateCbor.Serializer());
    module.addDeserializer(UnicityTreeCertificate.class, new UnicityTreeCertificateCbor.Deserializer());
    module.addSerializer(UnicityTreeCertificate.HashStep.class, new UnicityTreeCertificateHashStepCbor.Serializer());
    module.addDeserializer(UnicityTreeCertificate.HashStep.class, new UnicityTreeCertificateHashStepCbor.Deserializer());
    module.addSerializer(UnicitySeal.class, new UnicitySealCbor.Serializer());
    module.addDeserializer(UnicitySeal.class, new UnicitySealCbor.Deserializer());

    ObjectMapper objectMapper = new CBORMapper();
    objectMapper.registerModule(new Jdk8Module());
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

    module.addSerializer(SparseMerkleTreePath.class, new SparseMerkleTreePathJson.Serializer());
    module.addDeserializer(SparseMerkleTreePath.class, new SparseMerkleTreePathJson.Deserializer());

    module.addSerializer(SparseMerkleTreePathStep.class, new SparseMerkleTreePathStepJson.Serializer());
    module.addDeserializer(SparseMerkleTreePathStep.class,
        new SparseMerkleTreePathStepJson.Deserializer());

    module.addSerializer(SparseMerkleTreePathStep.Branch.class,
        new SparseMerkleTreePathStepBranchJson.Serializer());
    module.addDeserializer(SparseMerkleTreePathStep.Branch.class,
        new SparseMerkleTreePathStepBranchJson.Deserializer());

    module.addSerializer(SparseMerkleSumTreePath.class,
        new SparseMerkleSumTreePathJson.Serializer());
    module.addDeserializer(SparseMerkleSumTreePath.class,
        new SparseMerkleSumTreePathJson.Deserializer());

    module.addSerializer(SparseMerkleSumTreePathStep.class,
        new SparseMerkleSumTreePathStepJson.Serializer());
    module.addDeserializer(SparseMerkleSumTreePathStep.class,
        new SparseMerkleSumTreePathStepJson.Deserializer());

    module.addSerializer(SparseMerkleSumTreePathStep.Branch.class,
        new SparseMerkleSumTreePathStepBranchJson.Serializer());
    module.addDeserializer(SparseMerkleSumTreePathStep.Branch.class,
        new SparseMerkleSumTreePathStepBranchJson.Deserializer());

    module.addSerializer(Authenticator.class, new AuthenticatorJson.Serializer());
    module.addDeserializer(Authenticator.class, new AuthenticatorJson.Deserializer());

    module.addSerializer(RequestId.class, new RequestIdJson.Serializer());
    module.addDeserializer(RequestId.class, new RequestIdJson.Deserializer());

    module.addSerializer(Commitment.class, new CommitmentJson.Serializer());
    module.addDeserializer(MintCommitment.class, new MintCommitmentJson.Deserializer());
    module.addDeserializer(TransferCommitment.class, new TransferCommitmentJson.Deserializer());

    module.addSerializer(Token.class, new TokenJson.Serializer());
    module.addDeserializer(Token.class, new TokenJson.Deserializer());

    module.addSerializer(TokenState.class, new TokenStateJson.Serializer());
    module.addDeserializer(TokenState.class, new TokenStateJson.Deserializer());

    module.addSerializer(SerializablePredicate.class, new SerializablePredicateJson.Serializer());
    module.addDeserializer(SerializablePredicate.class, new SerializablePredicateJson.Deserializer());

    module.addSerializer(Transaction.class, new TransactionJson.Serializer());
    module.addDeserializer(Transaction.class, new TransactionJson.Deserializer());

    module.addSerializer(MintTransactionData.class, new MintTransactionDataJson.Serializer());
    module.addDeserializer(MintTransactionData.class, new MintTransactionDataJson.Deserializer());

    module.addSerializer(TransferTransactionData.class,
        new TransferTransactionDataJson.Serializer());
    module.addDeserializer(TransferTransactionData.class,
        new TransferTransactionDataJson.Deserializer());

    module.addDeserializer(MintTransactionReason.class,
        new MintTransactionReasonJson.Deserializer());

    module.addSerializer(SplitMintReason.class, new SplitMintReasonJson.Serializer());
    module.addDeserializer(SplitMintReason.class, new SplitMintReasonJson.Deserializer());

    module.addSerializer(SplitMintReasonProof.class, new SplitMintReasonProofJson.Serializer());
    module.addDeserializer(SplitMintReasonProof.class, new SplitMintReasonProofJson.Deserializer());

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
