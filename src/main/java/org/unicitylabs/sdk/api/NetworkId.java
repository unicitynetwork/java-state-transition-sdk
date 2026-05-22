package org.unicitylabs.sdk.api;

/**
 * Unicity network identifier ({@code α}). Used to scope token ids and other
 * network-bound values so they cannot be replayed across networks.
 *
 * <p>{@code 0} is reserved as an uninitialized sentinel and is rejected by
 * {@link #fromId(short)}.
 */
public enum NetworkId {
  MAINNET((short) 1),
  TESTNET((short) 2),
  LOCAL((short) 3);

  private final short id;

  NetworkId(short id) {
    this.id = id;
  }

  /**
   * Get the numeric network identifier.
   *
   * @return numeric identifier
   */
  public short getId() {
    return this.id;
  }

  /**
   * Look up a NetworkId by its numeric identifier.
   *
   * @param id numeric network identifier
   * @return matching network identifier
   * @throws IllegalArgumentException if {@code id} is not a registered network
   */
  public static NetworkId fromId(short id) {
    for (NetworkId network : NetworkId.values()) {
      if (network.id == id) {
        return network;
      }
    }
    throw new IllegalArgumentException("Unknown network identifier: " + id);
  }
}
