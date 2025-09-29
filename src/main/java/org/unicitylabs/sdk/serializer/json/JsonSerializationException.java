package org.unicitylabs.sdk.serializer.json;

/**
 * Json serialization exception for json errors.
 */
public class JsonSerializationException extends RuntimeException {

  /**
   * Create serialization exception from class and cause.
   *
   * @param c     class
   * @param cause cause
   */
  public JsonSerializationException(Class<?> c, Throwable cause) {
    super(c.getName(), cause);
  }

  /**
   * Create serialization exception from cause.
   *
   * @param cause cause
   */
  public JsonSerializationException(Throwable cause) {
    super(cause);
  }

  /**
   * Create serialization exception from message.
   *
   * @param message error
   */
  public JsonSerializationException(String message) {
    super(message);
  }
}
