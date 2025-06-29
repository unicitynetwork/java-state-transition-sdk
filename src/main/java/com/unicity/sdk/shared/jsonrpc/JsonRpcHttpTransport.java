
package com.unicity.sdk.shared.jsonrpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * JSON-RPC HTTP service.
 */
public class JsonRpcHttpTransport {
    private final String url;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * JSON-RPC HTTP service constructor.
     */
    public JsonRpcHttpTransport(String url) {
        this.url = url;
        this.httpClient = new OkHttpClient();
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
        
        CompletableFuture<Object> future = new CompletableFuture<>();
        
        try {
            String requestBody = objectMapper.writeValueAsString(requestMap);
            
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(requestBody, JSON))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful()) {
                            String body = responseBody != null ? responseBody.string() : "";
                            future.completeExceptionally(new JsonRpcNetworkError(response.code(), body));
                            return;
                        }
                        
                        String body = responseBody != null ? responseBody.string() : "";
                        JsonRpcResponse data = objectMapper.readValue(body, JsonRpcResponse.class);
                        
                        if (data.getError() != null) {
                            future.completeExceptionally(new JsonRpcDataError(data.getError()));
                            return;
                        }
                        
                        future.complete(data.getResult());
                    } catch (Exception e) {
                        future.completeExceptionally(new RuntimeException("Failed to parse JSON-RPC response", e));
                    }
                }
            });
            
            return future;
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
