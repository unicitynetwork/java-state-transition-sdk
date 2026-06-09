package org.unicitylabs.sdk.payment;

/**
 * Thrown when two split requests derive the same token id, which would collide in the sum trees.
 */
public class DuplicateSplitTokenIdException extends RuntimeException {

  /**
   * Create exception indicating that the given token id is shared by multiple split requests.
   *
   * @param tokenId duplicated token id description
   */
  public DuplicateSplitTokenIdException(String tokenId) {
    super("Duplicate token id across split requests: " + tokenId + ".");
  }
}
