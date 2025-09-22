
package org.unicitylabs.sdk.jsonrpc;

import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

/**
 * JSON-RPC HTTP service.
 */
public class JsonRpcHttpTransport {

    private static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final String HTTP_RETRY_AFTER = "Retry-After";

    private final String url;
  private final OkHttpClient httpClient;

  /**
   * JSON-RPC HTTP service constructor.
   */
  public JsonRpcHttpTransport(String url) {
    this.url = url;
    this.httpClient = new OkHttpClient();
  }

  /**
   * Send a JSON-RPC request.
   */
  public <T> CompletableFuture<T> request(String method, Object params, Class<T> resultType) {
    return request(method, params, resultType, null);
  }

  /**
   * Send a JSON-RPC request with optional API key.
   */
  public <T> CompletableFuture<T> request(String method, Object params, Class<T> resultType, String apiKey) {
    CompletableFuture<T> future = new CompletableFuture<>();

    try {
      Request.Builder requestBuilder = new Request.Builder()
          .url(this.url)
          .post(
              RequestBody.create(
                  UnicityObjectMapper.JSON.writeValueAsString(new JsonRpcRequest(method, params)),
                  JsonRpcHttpTransport.MEDIA_TYPE_JSON)
          );
      
      if (apiKey != null) {
        requestBuilder.header("Authorization", "Bearer " + apiKey);
      }
      
      Request request = requestBuilder.build();

      this.httpClient.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          future.completeExceptionally(e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
          try (ResponseBody body = response.body()) {
            if (!response.isSuccessful()) {
              String error = body != null ? body.string() : "";
              
              if (response.code() == HTTP_UNAUTHORIZED) {
                future.completeExceptionally(new UnauthorizedException(
                    "Unauthorized: Invalid or missing API key"));
                return;
              } else if (response.code() == HTTP_TOO_MANY_REQUESTS) {
                int retryAfterSeconds = extractRetryAfterSeconds(response);
                future.completeExceptionally(new RateLimitExceededException(
                    "Rate limit exceeded. Please retry after " + retryAfterSeconds + " seconds",
                    retryAfterSeconds));
                return;
              } else {
                  future.completeExceptionally(new JsonRpcNetworkError(response.code(), error));
                  return;
              }
            }

            JsonRpcResponse<T> data = UnicityObjectMapper.JSON.readValue(
                body != null ? body.string() : "",
                UnicityObjectMapper.JSON.getTypeFactory()
                    .constructParametricType(JsonRpcResponse.class, resultType));

            if (data.getError() != null) {
              future.completeExceptionally(new JsonRpcDataError(data.getError()));
              return;
            }

            future.complete(data.getResult());
          } catch (Exception e) {
            future.completeExceptionally(
                new RuntimeException("Failed to parse JSON-RPC response", e));
          }
        }
      });
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }

    return future;
  }

  private int extractRetryAfterSeconds(Response response) {
    String retryAfterHeader = response.header(HTTP_RETRY_AFTER);
    if (retryAfterHeader != null) {
      try {
        return Integer.parseInt(retryAfterHeader);
      } catch (NumberFormatException ignored) {
      }
    }
    // Default to 60 seconds if the HTTP header is missing, e.g. if the response is coming from a different component that is not using this header.
    return 60;
  }
}
