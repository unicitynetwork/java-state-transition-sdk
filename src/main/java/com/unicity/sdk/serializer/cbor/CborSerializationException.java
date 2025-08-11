package com.unicity.sdk.serializer.cbor;

public class CborSerializationException extends RuntimeException {
  public CborSerializationException(String message, Throwable cause) {
    super(message, cause);
  }

  public CborSerializationException(Throwable cause) {
    super(cause);
  }
}
