package org.unicitylabs.sdk.hash;

/**
 * Hash algorithm representation.
 */
public enum HashAlgorithm {
  /**
   * SHA2-256 hash algorithm.
   */
  SHA256(0, "SHA-256"),
  /**
   * SHA2-224 hash algorithm.
   */
  SHA224(1, "SHA-224"),
  /**
   * SHA2-384 hash algorithm.
   */
  SHA384(2, "SHA-384"),
  /**
   * SHA2-512 hash algorithm.
   */
  SHA512(3, "SHA-512"),
  /**
   * RIPEMD160 hash algorithm.
   */
  RIPEMD160(4, "RIPEMD160");

  private final int value;
  private final String algorithm;

  HashAlgorithm(int value, String algorithm) {
    this.value = value;
    this.algorithm = algorithm;
  }

  /**
   * Hash algorithm value in imprint.
   *
   * @return value
   */
  public int getValue() {
    return value;
  }

  /**
   * Hash algorithm string representation.
   *
   * @return algorithm
   */
  public String getAlgorithm() {
    return this.algorithm;
  }

  /**
   * Get HashAlgorithm from its numeric value.
   *
   * @param value The numeric value
   * @return The corresponding HashAlgorithm
   * @throws IllegalArgumentException if value is not valid
   */
  public static HashAlgorithm fromValue(int value) {
    for (HashAlgorithm algorithm : HashAlgorithm.values()) {
      if (algorithm.getValue() == value) {
        return algorithm;
      }
    }
    throw new IllegalArgumentException("Invalid HashAlgorithm value: " + value);
  }
}