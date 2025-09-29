package org.unicitylabs.sdk.serializer.cbor;

/**
 * CBOR major types.
 */
public enum CborMajorType {
  UNSIGNED_INTEGER(0b00000000),
  NEGATIVE_INTEGER(0b00100000),
  BYTE_STRING(0b01000000),
  TEXT_STRING(0b01100000),
  ARRAY(0b10000000),
  MAP(0b10100000),
  TAG(0b11000000),
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