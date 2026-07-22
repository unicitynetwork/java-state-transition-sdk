package org.unicitylabs.sdk.transaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TokenTypeTest {

  @Test
  void rejectsTypesOutsideAllowedRange() {
    Assertions.assertThrows(IllegalArgumentException.class,
            () -> new TokenType(new byte[TokenType.MIN_LENGTH - 1]));
    Assertions.assertThrows(IllegalArgumentException.class,
            () -> new TokenType(new byte[TokenType.MAX_LENGTH + 1]));
  }

  @Test
  void acceptsTypesWithinAllowedRange() {
    for (int length : new int[] {TokenType.MIN_LENGTH, 32, TokenType.MAX_LENGTH}) {
      TokenType type = new TokenType(new byte[length]);
      Assertions.assertEquals(type, TokenType.fromCbor(type.toCbor()));
    }
  }
}
