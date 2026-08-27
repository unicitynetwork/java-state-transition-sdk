package org.unicitylabs.sdk.interop;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.transaction.Token;

/**
 * The producing half of the cross-SDK interop vectors: this SDK's token, pinned.
 *
 * <p>Regenerate with {@code ./gradlew test -Dinterop.write=true}. The committed bytes are what the
 * TypeScript SDK's consuming test reads, so changing them is a deliberate act: if this test fails,
 * either the wire format changed — in which case the TypeScript vectors move with it — or
 * something that was supposed to be deterministic is not.
 */
class InteropVectorTest {

  private static final String TOKEN = "java-token-v2.cbor";
  private static final String TRUST_BASE = "java-token-v2.trust-base.json";

  @Test
  void tokenMatchesTheCommittedVector() throws Exception {
    Token token = InteropFixture.buildToken();
    byte[] encoded = token.toCbor();

    if (Boolean.getBoolean("interop.write")) {
      InteropFixture.write(TOKEN, encoded);
      InteropFixture.write(TRUST_BASE,
              InteropFixture.trustBaseJson().getBytes(StandardCharsets.UTF_8));
      return;
    }

    Assertions.assertTrue(Files.exists(InteropFixture.VECTORS.resolve(TOKEN)),
            "missing vector; regenerate with -Dinterop.write=true");
    Assertions.assertEquals(
            InteropFixture.hex(InteropFixture.read(TOKEN)),
            InteropFixture.hex(encoded),
            "regenerated token does not match the committed interop vector");
  }

  @Test
  void tokenRoundTripsThroughItsOwnEncoding() throws Exception {
    Token token = InteropFixture.buildToken();

    Assertions.assertEquals(
            InteropFixture.hex(token.toCbor()),
            InteropFixture.hex(Token.fromCbor(token.toCbor()).toCbor()));
  }
}
