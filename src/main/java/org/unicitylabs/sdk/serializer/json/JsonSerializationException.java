package org.unicitylabs.sdk.serializer.json;

public class JsonSerializationException extends RuntimeException {
  public JsonSerializationException(Class<?> c, Throwable cause) {
    super(c.getName(), cause);
  }

  public JsonSerializationException(Throwable cause) {
    super(cause);
  }

  public JsonSerializationException(String message) {
    super(message);
  }
}
