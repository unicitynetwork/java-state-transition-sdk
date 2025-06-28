package com.unicity.sdk.shared.jsonrpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonRpcHttpTransportTest {
    
    @Test
    public void testJsonRpcResponse() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        // Test successful response
        String successJson = "{\"jsonrpc\":\"2.0\",\"result\":\"test result\",\"id\":\"123\"}";
        JsonRpcResponse successResponse = mapper.readValue(successJson, JsonRpcResponse.class);
        
        assertEquals("2.0", successResponse.getJsonrpc());
        assertEquals("test result", successResponse.getResult());
        assertNull(successResponse.getError());
        assertEquals("123", successResponse.getId());
        
        // Test error response
        String errorJson = "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32600,\"message\":\"Invalid Request\"},\"id\":null}";
        JsonRpcResponse errorResponse = mapper.readValue(errorJson, JsonRpcResponse.class);
        
        assertEquals("2.0", errorResponse.getJsonrpc());
        assertNull(errorResponse.getResult());
        assertNotNull(errorResponse.getError());
        assertEquals(-32600, errorResponse.getError().getCode());
        assertEquals("Invalid Request", errorResponse.getError().getMessage());
        assertNull(errorResponse.getId());
    }
    
    @Test
    public void testJsonRpcDataError() {
        JsonRpcError error = new JsonRpcError(-32700, "Parse error");
        JsonRpcDataError dataError = new JsonRpcDataError(error);
        
        assertEquals(-32700, dataError.getCode());
        assertEquals("Parse error", dataError.getMessage());
        assertEquals(error, dataError.getError());
    }
    
    @Test
    public void testJsonRpcNetworkError() {
        JsonRpcNetworkError networkError = new JsonRpcNetworkError(404, "Not Found");
        
        assertEquals(404, networkError.getStatus());
        assertEquals("Not Found", networkError.getResponseText());
        assertTrue(networkError.getMessage().contains("404"));
        assertTrue(networkError.getMessage().contains("Not Found"));
    }
}