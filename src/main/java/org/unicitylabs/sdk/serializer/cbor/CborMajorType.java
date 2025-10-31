package org.unicitylabs.sdk.serializer.cbor;

/**
 * CBOR major types with masks.
 */
public enum CborMajorType {
  /**
   * Unsigned integer major type.
   */
  UNSIGNED_INTEGER(0b00000000),
  /**
   * Negative integer major type.
   */
  NEGATIVE_INTEGER(0b00100000),
  /**
   * Byte string major type.
   */
  BYTE_STRING(0b01000000),
  /**
   * Text string major type.
   */
  TEXT_STRING(0b01100000),
  /**
   * Array major type.
   */
  ARRAY(0b10000000),
  /**
   * Map major type.
   */
  MAP(0b10100000),
  /**
   * CBOR tag major type.
   */
  TAG(0b11000000),
  /**
   * Special types and floating point major type.
   */
  SIMPLE_AND_FLOAT(0b11100000);

  private final int type;

  CborMajorType(int type) {
    this.type = type;
  }

  /**
   * Get CBOR major type.
   *
   * @return type
   */
  public int getType() {
    return this.type;
  }

  /**
   * Get CBOR major type from value.
   *
   * @param type value
   * @return type
   */
  public static CborMajorType fromType(int type) {
    for (CborMajorType majorType : CborMajorType.values()) {
      if (Integer.compareUnsigned(majorType.getType(), type & 0xFF) == 0) {
        return majorType;
      }
    }
    throw new IllegalArgumentException("Invalid CBOR major type: " + type);
  }

}