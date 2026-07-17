package org.unicitylabs.sdk.smt;

/**
 * Exception thrown when a sparse Merkle tree insertion targets a key that is already present in the
 * tree.
 */
public class LeafExistsException extends Exception {

  /**
   * Create exception indicating that a leaf already exists for the inserted key.
   */
  public LeafExistsException() {
    super("Leaf already exists.");
  }
}
