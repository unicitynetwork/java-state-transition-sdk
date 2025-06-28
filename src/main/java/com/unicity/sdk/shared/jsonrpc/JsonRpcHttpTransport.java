
package com.unicity.sdk.shared.jsonrpc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class JsonRpcHttpTransport {
    private final String url;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public JsonRpcHttpTransport(String url) {
        this.url = url;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<IJsonRpcResponse> send(String method, Object params) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "jsonrpc", "2.0",
                    "method", method,
                    "params", params,
                    "id", 1
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        try {
                            if (response.statusCode() != 200) {
                                throw new JsonRpcNetworkError("Network error: " + response.statusCode());
                            }
                            return objectMapper.readValue(response.body(), IJsonRpcResponse.class);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
