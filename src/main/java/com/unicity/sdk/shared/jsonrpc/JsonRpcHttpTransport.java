
package com.unicity.sdk.shared.jsonrpc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * JSON-RPC HTTP service.
 */
public class JsonRpcHttpTransport {
    private final String url;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * JSON-RPC HTTP service constructor.
     */
    public JsonRpcHttpTransport(String url) {
        this.url = url;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Send a JSON-RPC request.
     */
    public CompletableFuture<Object> request(String method, Object params) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("id", UUID.randomUUID().toString());
        requestMap.put("jsonrpc", "2.0");
        requestMap.put("method", method);
        if (params != null) {
            requestMap.put("params", params);
        }
        
        try {
            String requestBody = objectMapper.writeValueAsString(requestMap);
            System.out.println("JSON-RPC Request: " + requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        try {
                            if (response.statusCode() != 200) {
                                throw new JsonRpcNetworkError(response.statusCode(), response.body());
                            }
                            
                            JsonRpcResponse data = objectMapper.readValue(response.body(), JsonRpcResponse.class);
                            
                            if (data.getError() != null) {
                                throw new JsonRpcDataError(data.getError());
                            }
                            
                            return data.getResult();
                        } catch (JsonRpcNetworkError | JsonRpcDataError e) {
                            throw new RuntimeException(e);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse JSON-RPC response", e);
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
