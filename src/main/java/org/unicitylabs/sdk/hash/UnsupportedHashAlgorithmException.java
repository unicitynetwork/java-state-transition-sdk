
package org.unicitylabs.sdk.hash;

/**
 * Throw given error when hash algorithm is not supported by data hasher.
 */
public class UnsupportedHashAlgorithmException extends RuntimeException {

  private final HashAlgorithm algorithm;

  /**
   * Create exception for given hash algorithm.
   *
   * @param algorithm algorithm
   */
  public UnsupportedHashAlgorithmException(HashAlgorithm algorithm) {
    super("Unsupported hash algorithm: " + algorithm);
    this.algorithm = algorithm;
  }

  /**
   * Get algorithm which was not supported.
   *
   * @return algorithm
   */
  public HashAlgorithm getAlgorithm() {
    return algorithm;
  }
}
