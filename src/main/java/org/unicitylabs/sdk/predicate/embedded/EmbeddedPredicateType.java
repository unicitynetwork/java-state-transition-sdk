
package org.unicitylabs.sdk.predicate.embedded;

import java.util.Arrays;

/**
 * Embedded predicate types.
 */
public enum EmbeddedPredicateType {
  /**
   * Unmasked predicate type.
   */
  UNMASKED(new byte[]{0x0}),
  /**
   * Masked predicate type.
   */
  MASKED(new byte[]{0x1}),
  /**
   * Burn predicate type.
   */
  BURN(new byte[]{0x2});

  private final byte[] bytes;

  EmbeddedPredicateType(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Get embedded predicate encoded code bytes.
   *
   * @return encoded code bytes
   */
  public byte[] getBytes() {
    return Arrays.copyOf(this.bytes, this.bytes.length);
  }

  /**
   * Create embedded predicate type from bytes.
   *
   * @param bytes predicate type bytes
   * @return predicate type
   */
  public static EmbeddedPredicateType fromBytes(byte[] bytes) {
    for (EmbeddedPredicateType type : EmbeddedPredicateType.values()) {
      if (Arrays.equals(bytes, type.getBytes())) {
        return type;
      }
    }

    throw new RuntimeException("Invalid embedded predicate type");
  }

}
