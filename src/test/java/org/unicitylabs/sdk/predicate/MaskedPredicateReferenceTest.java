package org.unicitylabs.sdk.predicate;

import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.token.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaskedPredicateReferenceTest {

  @Test
  void testReferenceAddress() throws Exception {
    Assertions.assertEquals(
        "DIRECT://000095ca469ab7a37f8be976b7da1a6023369182ba3cdd1293c07a4b2bf40aa5118d60f5bf36",
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