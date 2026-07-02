package org.unicitylabs.sdk.crypto.secp256k1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Recovery-id validation for {@link Signature#decode} (L-01), mirroring the JS
 * {@code SignatureTest}.
 */
public class SignatureTest {

  private static byte[] sigBytes(int recovery) {
    byte[] bytes = new byte[65];
    bytes[64] = (byte) recovery;
    return bytes;
  }

  @Test
  public void rejectsRecoveryIdOutOfRange() {
    Assertions.assertTrue(Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> Signature.decode(sigBytes(4))).getMessage().contains("Invalid signature recovery id"));
    Assertions.assertTrue(Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> Signature.decode(sigBytes(255))).getMessage().contains("Invalid signature recovery id"));
  }

  @Test
  public void acceptsRecoveryIdsZeroThroughThree() {
    for (int recovery = 0; recovery <= 3; recovery++) {
      Assertions.assertEquals(recovery, Signature.decode(sigBytes(recovery)).getRecovery());
    }
  }

  @Test
  public void rejectsInputThatIsNot65Bytes() {
    Assertions.assertThrows(
            IllegalArgumentException.class, () -> Signature.decode(new byte[64]));
  }
}
