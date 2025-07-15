package com.unicity.sdk.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.smt.path.MerkleTreePath;
import org.junit.jupiter.api.Test;

import java.util.List;

public class InclusionProofTest {
    @Test
    public void testInclusionProofJsonSerialization() throws Exception {
        DataHash transactionHash = new DataHash(HashAlgorithm.SHA256, new byte[32]);
        Authenticator.create()

        InclusionProof inclusionProof = new InclusionProof(new MerkleTreePath(DataHash.fromImprint(new byte[34]), List.of()), null, null);
        System.out.println(new ObjectMapper().writeValueAsString(inclusionProof));
    }
}
