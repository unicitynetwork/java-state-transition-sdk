package org.unicitylabs.sdk.api;

/**
 * Unicity network identifier ({@code α}). Used to scope token ids and other
 * network-bound values so they cannot be replayed across networks.
 */
public final class NetworkId {

  public static final NetworkId MAINNET = new NetworkId((short) 1, "MAINNET");
  public static final NetworkId TESTNET = new NetworkId((short) 2, "TESTNET");
  public static final NetworkId LOCAL = new NetworkId((short) 3, "LOCAL");

  private final short id;
  private final String name;

  private NetworkId(short id, String name) {
    this.id = id;
    this.name = name;
  }

  private NetworkId(short id) {
    this(id, null);
  }

  /**
   * Resolve a NetworkId from its numeric identifier. Returns the registered
   * singleton for known ids; constructs a new (unnamed) instance for any
   * other 16-bit value.
   *
   * @param id numeric network identifier
   * @return NetworkId for the given identifier
   */
  public static NetworkId fromId(short id) {
    if (id <= 0) {
      throw new IllegalArgumentException(
          "Network identifier out of allowed 16-bit unsigned range: " + id + ".");
    }
    if (id == MAINNET.id) {
      return MAINNET;
    }
    if (id == TESTNET.id) {
      return TESTNET;
    }
    if (id == LOCAL.id) {
      return LOCAL;
    }
    return new NetworkId(id);
  }

  /**
   * Get the numeric network identifier.
   *
   * @return numeric identifier
   */
  public short getId() {
    return this.id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NetworkId)) {
      return false;
    }
    return this.id == ((NetworkId) o).id;
  }

  @Override
  public int hashCode() {
    return Short.hashCode(this.id);
  }

  @Override
  public String toString() {
    return "NetworkId[" + (this.name != null ? this.name : this.id) + "]";
  }
}