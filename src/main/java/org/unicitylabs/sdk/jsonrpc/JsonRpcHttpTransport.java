
package org.unicitylabs.sdk.jsonrpc;

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
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;

/**
 * JSON-RPC HTTP service.
 */
public class JsonRpcHttpTransport {

  private static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");

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
    CompletableFuture<T> future = new CompletableFuture<>();

    try {
      Request request = new Request.Builder()
          .url(this.url)
          .post(
              RequestBody.create(
                  UnicityObjectMapper.JSON.writeValueAsString(
                      new JsonRpcRequest(method, params)
                  ),
                  JsonRpcHttpTransport.MEDIA_TYPE_JSON
              )
          )
          .build();

      this.httpClient.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          future.completeExceptionally(e);
        }

        @Override
        public void onResponse(Call call, Response response) {
          try (ResponseBody body = response.body()) {
            if (!response.isSuccessful()) {
              String error = body != null ? body.string() : "";
              future.completeExceptionally(new JsonRpcNetworkException(response.code(), error));
              return;
            }

            JsonRpcResponse<T> data = UnicityObjectMapper.JSON.readValue(
                body != null ? body.string() : "",
                UnicityObjectMapper.JSON.getTypeFactory()
                    .constructParametricType(JsonRpcResponse.class, resultType)
            );

            if (data.getError() != null) {
              future.completeExceptionally(
                  new JsonRpcNetworkException(
                      data.getError().getCode(),
                      data.getError().getMessage()
                  )
              );
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
}
