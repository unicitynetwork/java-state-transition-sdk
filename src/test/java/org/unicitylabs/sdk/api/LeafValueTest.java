package org.unicitylabs.sdk.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.util.HexConverter;

public class LeafValueTest {

  // Shared across the Go, Rust and TypeScript implementations: the leaf value is SHA-256 over
  // the deterministic CBOR array [transactionHash, referenceTime].
  private static final DataHash TRANSACTION_HASH = new DataHash(
          HashAlgorithm.SHA256,
          HexConverter.decode("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f")
  );
  private static final long REFERENCE_TIME = 1755000000L;
  private static final String EXPECTED =
          "0235bd52cfa10c9785dfa01942bc396f201fe715dbc3896ee117a97e895e1e36";

  @Test
  public void matchesTheSharedTestVector() {
    Assertions.assertEquals(
            EXPECTED,
            HexConverter.encode(LeafValue.calculate(TRANSACTION_HASH, REFERENCE_TIME).getData()));
  }

  @Test
  public void changesWithTheReferenceTime() {
    Assertions.assertNotEquals(
            EXPECTED,
            HexConverter.encode(
                    LeafValue.calculate(TRANSACTION_HASH, REFERENCE_TIME + 1).getData()));
  }
}
