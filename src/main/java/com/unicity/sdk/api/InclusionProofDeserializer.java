package com.unicity.sdk.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.smt.MerkleTreePath;
import com.unicity.sdk.shared.smt.MerkleTreePathStep;
import com.unicity.sdk.shared.util.HexConverter;
import com.unicity.sdk.transaction.InclusionProof;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom deserializer for InclusionProof that handles the aggregator's JSON structure.
 * This maps the TypeScript SDK structure to our Java structure.
 */
public class InclusionProofDeserializer {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static InclusionProof deserialize(Object result) throws JsonProcessingException {
        if (!(result instanceof Map)) {
            throw new IllegalArgumentException("Expected Map for InclusionProof, got: " + 
                (result != null ? result.getClass().getName() : "null"));
        }
        
        Map<String, Object> map = (Map<String, Object>) result;
        
        // Extract authenticator (may be null)
        Authenticator authenticator = null;
        Object authObj = map.get("authenticator");
        if (authObj != null && authObj instanceof Map) {
            authenticator = objectMapper.convertValue(authObj, Authenticator.class);
        }
        
        // Extract transactionHash (may be null)
        DataHash transactionHash = null;
        Object txHashObj = map.get("transactionHash");
        if (txHashObj != null && txHashObj instanceof String) {
            transactionHash = DataHash.fromImprint(HexConverter.decode((String) txHashObj));
        }
        
        // Extract merkleTreePath
        Object pathObj = map.get("merkleTreePath");
        if (!(pathObj instanceof Map)) {
            throw new IllegalArgumentException("Expected Map for merkleTreePath");
        }
        
        Map<String, Object> pathMap = (Map<String, Object>) pathObj;
        
        // Extract root (required)
        String rootHex = (String) pathMap.get("root");
        DataHash root = DataHash.fromImprint(HexConverter.decode(rootHex));
        
        // Extract steps
        List<Map<String, Object>> stepsList = (List<Map<String, Object>>) pathMap.get("steps");
        List<MerkleTreePathStep> steps = new ArrayList<>();
        
        for (Map<String, Object> stepMap : stepsList) {
            // TypeScript SDK uses: path (bigint as string), sibling (hash or null), branch (array or null)
            // We need to map this to our Java structure which uses: hash, isRight
            
            String pathStr = (String) stepMap.get("path");
            Object siblingObj = stepMap.get("sibling");
            Object branchObj = stepMap.get("branch");
            
            // For now, create a simple mapping
            // In the actual implementation, we'd need to properly handle the path/sibling/branch structure
            // This is a placeholder that creates dummy steps
            DataHash stepHash = null;
            boolean isRight = false;
            
            if (siblingObj != null && siblingObj instanceof String) {
                stepHash = DataHash.fromImprint(HexConverter.decode((String) siblingObj));
                // Determine isRight based on path bit
                if (pathStr != null) {
                    BigInteger path = new BigInteger(pathStr);
                    // Check least significant bit to determine if right
                    isRight = path.testBit(0);
                }
            }
            
            if (stepHash != null) {
                steps.add(new MerkleTreePathStep(stepHash, isRight));
            }
        }
        
        // Create MerkleTreePath
        // Note: Our Java MerkleTreePath doesn't have a root field, so we'll need to update it
        MerkleTreePath merkleTreePath = new MerkleTreePath(steps);
        
        return new InclusionProof(merkleTreePath, authenticator, transactionHash);
    }
}