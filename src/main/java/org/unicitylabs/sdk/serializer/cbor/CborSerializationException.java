package org.unicitylabs.sdk.serializer.cbor;

/**
 * CBOR serialization exception, when something goes wrong with deserializing or serializing.
 */
public class CborSerializationException extends RuntimeException {

  /**
   * Create CBOR serialization exception.
   *
   * @param message error message
   */
  public CborSerializationException(String message) {
    super(message);
  }
}
