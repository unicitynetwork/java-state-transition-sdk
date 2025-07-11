package com.unicity.sdk.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.smt.MerkleTreePath;
import com.unicity.sdk.shared.smt.MerkleTreePathStep;
import com.unicity.sdk.shared.smt.MerkleTreePathStepBranch;
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
        
        boolean isFirstStep = true;
        for (Map<String, Object> stepMap : stepsList) {
            // TypeScript SDK uses: path (bigint as string), sibling (hash or null), branch (array or null)
            
            String pathStr = (String) stepMap.get("path");
            Object siblingObj = stepMap.get("sibling");
            Object branchObj = stepMap.get("branch");
            
            // Parse path
            BigInteger path = pathStr != null ? new BigInteger(pathStr) : null;
            
            // Parse sibling
            DataHash sibling = null;
            if (siblingObj != null && siblingObj instanceof String) {
                sibling = DataHash.fromImprint(HexConverter.decode((String) siblingObj));
            }
            
            // Parse branch
            MerkleTreePathStepBranch branch = null;
            if (branchObj != null && branchObj instanceof List) {
                List<String> branchList = (List<String>) branchObj;
                if (!branchList.isEmpty()) {
                    byte[] branchValue = HexConverter.decode(branchList.get(0));
                    branch = new MerkleTreePathStepBranch(branchValue);
                } else {
                    branch = new MerkleTreePathStepBranch(null);
                }
            }
            
            // Create step - only first step gets the root
            MerkleTreePathStep step = new MerkleTreePathStep(
                isFirstStep ? root : null,
                sibling,
                path,
                branch
            );
            steps.add(step);
            isFirstStep = false;
        }
        
        // Create MerkleTreePath with root
        MerkleTreePath merkleTreePath = new MerkleTreePath(root, steps);
        
        return new InclusionProof(merkleTreePath, authenticator, transactionHash);
    }
}