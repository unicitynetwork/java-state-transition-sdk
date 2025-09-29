
package org.unicitylabs.sdk.predicate.embedded;

import java.util.Arrays;

/**
 * Embedded predicate types.
 */
public enum EmbeddedPredicateType {
  UNMASKED(new byte[]{0x0}),
  MASKED(new byte[]{0x1}),
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
