
package org.unicitylabs.sdk.predicate.embedded;

import java.util.Arrays;

public enum EmbeddedPredicateType {
    UNMASKED(new byte[] {0x0}),
    MASKED(new byte[] {0x1}),
    BURN(new byte[] {0x2});

  private final byte[] bytes;

  EmbeddedPredicateType(byte[] bytes) {
    this.bytes = bytes;
  }

  public byte[] getBytes() {
    return Arrays.copyOf(this.bytes, this.bytes.length);
  }

  public static EmbeddedPredicateType fromBytes(byte[] bytes) {
    for (EmbeddedPredicateType type : EmbeddedPredicateType.values()) {
      if (Arrays.equals(bytes, type.getBytes())) {
        return type;
      }
    }

    throw new RuntimeException("Invalid embedded predicate type");
  }

}
