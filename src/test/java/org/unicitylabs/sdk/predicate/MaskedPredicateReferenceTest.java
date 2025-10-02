package org.unicitylabs.sdk.predicate;

import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.embedded.MaskedPredicateReference;
import org.unicitylabs.sdk.token.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaskedPredicateReferenceTest {

  @Test
  void testReferenceAddress() {
    Assertions.assertEquals(
        "DIRECT://000056787e7ec9ef8e70cc715f061bd83981d552c6f813f9a319153e24321ccf5195f0f78200",
        MaskedPredicateReference.create(
                new TokenType(new byte[32]),
                "my_algorithm",
                new byte[32],
                HashAlgorithm.SHA256,
                new byte[3]
            )
            .toAddress()
            .getAddress());
  }
}