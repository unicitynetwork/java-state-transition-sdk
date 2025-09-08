package org.unicitylabs.sdk.token;

import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.predicate.Predicate;
import java.nio.charset.StandardCharsets;

public class NameTagTokenState extends TokenState {
  private final Address address;

  public NameTagTokenState(Predicate unlockPredicate, Address address) {
    super(unlockPredicate, address == null ? null : address.getAddress().getBytes(StandardCharsets.UTF_8));

    this.address = address;
  }

  public Address getAddress() {
    return address;
  }
}
