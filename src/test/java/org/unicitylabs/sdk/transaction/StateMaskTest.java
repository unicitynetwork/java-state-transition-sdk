package org.unicitylabs.sdk.transaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StateMaskTest {

  @Test
  void rejectsMasksOutsideAllowedRange() {
    Assertions.assertThrows(IllegalArgumentException.class,
            () -> StateMask.fromBytes(new byte[StateMask.MIN_LENGTH - 1]));
    Assertions.assertThrows(IllegalArgumentException.class,
            () -> StateMask.fromBytes(new byte[StateMask.MAX_LENGTH + 1]));
  }

  @Test
  void acceptsMasksWithinAllowedRange() {
    for (int length : new int[] {StateMask.MIN_LENGTH, StateMask.LENGTH, StateMask.MAX_LENGTH}) {
      StateMask mask = StateMask.fromBytes(new byte[length]);
      Assertions.assertEquals(mask, StateMask.fromCbor(mask.toCbor()));
      Assertions.assertEquals(length, mask.getBytes().length);
    }
  }

  @Test
  void generatesDefaultLengthMask() {
    Assertions.assertEquals(StateMask.LENGTH, StateMask.generate().getBytes().length);
  }
}
