package com.unicity.sdk.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.LeafValue;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.signing.SigningService;
import com.unicity.sdk.shared.smt.SparseMerkleTree;
import com.unicity.sdk.shared.smt.path.MerkleTreePath;
import com.unicity.sdk.shared.util.HexConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InclusionProofTest {
    @Test
    public void testInclusionProofJsonSerialization() throws Exception {
        SigningService signingService = new SigningService(HexConverter.decode("0000000000000000000000000000000000000000000000000000000000000001"));

        DataHash transactionHash = new DataHash(HashAlgorithm.SHA256, new byte[32]);
        Authenticator authenticator = Authenticator.create(signingService, transactionHash, new DataHash(HashAlgorithm.SHA256, new byte[32]));

        LeafValue leaf = LeafValue.create(authenticator, transactionHash);
        RequestId requestId = RequestId.create(signingService.getPublicKey(), authenticator.getStateHash());

        SparseMerkleTree smt = new SparseMerkleTree(HashAlgorithm.SHA256);
        smt.addLeaf(requestId.toBitString().toBigInteger(), leaf.getBytes());

        MerkleTreePath merkleTreePath = smt.calculateRoot().getPath(requestId.toBitString().toBigInteger());

        InclusionProof inclusionProof = new InclusionProof(merkleTreePath, authenticator, transactionHash);
        Assertions.assertEquals(inclusionProof, UnicityObjectMapper.JSON.readValue(UnicityObjectMapper.JSON.writeValueAsString(inclusionProof), InclusionProof.class));
    }
}
