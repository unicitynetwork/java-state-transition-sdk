package com.unicity.sdk.token;

import com.unicity.sdk.address.Address;
import com.unicity.sdk.predicate.Predicate;
import java.nio.charset.StandardCharsets;

public class NameTagTokenState extends TokenState {
  private final Address address;

  public NameTagTokenState(Predicate unlockPredicate, Address address) {
    super(unlockPredicate, address.getAddress().getBytes(StandardCharsets.UTF_8));

    this.address = address;
  }

  public Address getAddress() {
    return address;
  }
}
