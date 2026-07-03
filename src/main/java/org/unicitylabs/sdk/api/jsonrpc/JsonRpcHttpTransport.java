
package org.unicitylabs.sdk.api.jsonrpc;

import okhttp3.*;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * JSON-RPC HTTP service.
 */
public class JsonRpcHttpTransport {

  /** Default maximum response body size in bytes. */
  public static final int DEFAULT_MAX_RESPONSE_BYTES = 8 * 1024 * 1024;

  private static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");

  private static final OkHttpClient DEFAULT_HTTP_CLIENT = new OkHttpClient.Builder()
          .followRedirects(false)
          .followSslRedirects(false)
          .build();

  private final String url;
  private final int maxResponseBytes;
  private final OkHttpClient httpClient;

  /**
   * JSON-RPC HTTP service constructor.
   *
   * @param url service URL
   */
  public JsonRpcHttpTransport(String url) {
    this(url, JsonRpcHttpTransport.DEFAULT_MAX_RESPONSE_BYTES);
  }

  /**
   * JSON-RPC HTTP service constructor.
   *
   * @param url service URL
   * @param maxResponseBytes maximum response body size in bytes
   */
  public JsonRpcHttpTransport(String url, int maxResponseBytes) {
    this(url, JsonRpcHttpTransport.DEFAULT_HTTP_CLIENT, maxResponseBytes);
  }

  /**
   * JSON-RPC HTTP service constructor with a caller-supplied HTTP client, to share a single
   * connection and thread pool across transports.
   *
   * @param url service URL
   * @param httpClient OkHttp client to use
   */
  public JsonRpcHttpTransport(String url, OkHttpClient httpClient) {
    this(url, httpClient, JsonRpcHttpTransport.DEFAULT_MAX_RESPONSE_BYTES);
  }

  /**
   * JSON-RPC HTTP service constructor with a caller-supplied HTTP client, to share a single
   * connection and thread pool across transports. Redirect following is always disabled on the
   * transport's client (via {@link OkHttpClient#newBuilder()}, which shares the supplied client's
   * connection pool and dispatcher) so authentication headers are never replayed to a redirect
   * target, regardless of the caller's redirect policy.
   *
   * @param url service URL
   * @param httpClient OkHttp client to use
   * @param maxResponseBytes maximum response body size in bytes
   */
  public JsonRpcHttpTransport(String url, OkHttpClient httpClient, int maxResponseBytes) {
    this.url = Objects.requireNonNull(url, "url cannot be null");
    this.httpClient = Objects.requireNonNull(httpClient, "httpClient cannot be null")
            .newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build();
    this.maxResponseBytes = maxResponseBytes;
  }

  /**
   * Send a JSON-RPC request.
   *
   * @param <T>        expected result type
   * @param method     JSON-RPC method
   * @param params     JSON-RPC parameters
   * @param resultType expected result type
   * @return future with result
   */
  public <T> CompletableFuture<T> request(String method, Object params, Class<T> resultType) {
    return this.request(method, params, resultType, Map.of());
  }

  /**
   * Send a JSON-RPC request with optional API key.
   *
   * @param <T>        expected result type
   * @param method     JSON-RPC method
   * @param params     JSON-RPC parameters
   * @param resultType expected result type
   * @param headers    additional HTTP headers
   * @return future with result
   */
  public <T> CompletableFuture<T> request(
          String method,
          Object params,
          Class<T> resultType,
          Map<String, List<String>> headers
  ) {
    Objects.requireNonNull(method, "method cannot be null");
    Objects.requireNonNull(resultType, "resultType cannot be null");
    Objects.requireNonNull(headers, "headers cannot be null");

    CompletableFuture<T> future = new CompletableFuture<>();

    try {
      JsonRpcRequest rpcRequest = new JsonRpcRequest(method, params);
      UUID requestId = rpcRequest.getId();

      Request.Builder requestBuilder = new Request.Builder()
              .url(this.url)
              .post(
                      RequestBody.create(
                              UnicityObjectMapper.JSON.writeValueAsString(rpcRequest),
                              JsonRpcHttpTransport.MEDIA_TYPE_JSON)
              );

      headers.forEach((header, values) ->
              values.forEach(value ->
                      requestBuilder.addHeader(header, value)));

      Request request = requestBuilder.build();

      this.httpClient.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          future.completeExceptionally(e);
        }

        @Override
        public void onResponse(Call call, Response response) {
          try (ResponseBody body = response.body()) {
            String bodyString = JsonRpcHttpTransport.this.readBounded(body);

            if (!response.isSuccessful()) {
              future.completeExceptionally(
                      new JsonRpcNetworkException(response.code(), bodyString));
              return;
            }

            JsonRpcResponse<T> data = UnicityObjectMapper.JSON.readValue(
                    bodyString,
                    UnicityObjectMapper.JSON.getTypeFactory()
                            .constructParametricType(JsonRpcResponse.class, resultType)
            );

            if (data.getError() != null) {
              future.completeExceptionally(new JsonRpcNetworkException(
                      data.getError().getCode(), data.getError().getMessage()));
              return;
            }

            if (!requestId.equals(data.getId())) {
              future.completeExceptionally(new IllegalArgumentException(
                      "JSON-RPC response id mismatch: expected " + requestId + ", got "
                              + data.getId() + "."));
              return;
            }

            future.complete(data.getResult());
          } catch (Exception e) {
            future.completeExceptionally(e);
          }
        }
      });
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }

    return future;
  }

  /**
   * Read the response body as a string, rejecting bodies larger than the configured limit before
   * buffering the whole payload.
   */
  private String readBounded(ResponseBody body) throws IOException {
    if (body == null) {
      return "";
    }

    try (InputStream in = body.byteStream()) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      int total = 0;
      int read;
      while ((read = in.read(buffer)) != -1) {
        total += read;
        if (total > this.maxResponseBytes) {
          throw new IOException("JSON-RPC response exceeds the maximum allowed size.");
        }
        out.write(buffer, 0, read);
      }

      // ByteArrayOutputStream.toString(Charset) is post-API-31; construct the String directly.
      return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
  }
}
