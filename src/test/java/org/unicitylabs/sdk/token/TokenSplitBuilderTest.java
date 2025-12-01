package org.unicitylabs.sdk.token;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.bft.UnicityCertificateUtils;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.BranchExistsException;
import org.unicitylabs.sdk.mtree.LeafOutOfBoundsException;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePathFixture;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.embedded.MaskedPredicate;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.InclusionProofFixture;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.transaction.MintTransactionFixture;
import org.unicitylabs.sdk.transaction.split.TokenSplitBuilder;
import org.unicitylabs.sdk.verification.VerificationException;

public class TokenSplitBuilderTest {

  private Token<?> createToken(TokenCoinData coinData) {
    SigningService signingService = new SigningService(SigningService.generatePrivateKey());
    UnicityCertificate unicityCertificate = UnicityCertificateUtils.generateCertificate(
        signingService, DataHash.fromImprint(new byte[34]));

    TokenId tokenId = new TokenId(new byte[10]);
    TokenType tokenType = new TokenType(new byte[10]);

    Predicate predicate = new MaskedPredicate(
        tokenId,
        tokenType,
        new byte[32],
        "secp256k1",
        HashAlgorithm.SHA256,
        new byte[32]
    );

    return new Token<>(
        new TokenState(predicate, null),
        MintTransactionFixture.create(
            new MintTransaction.Data<>(
                tokenId,
                tokenType,
                null,
                coinData,
                predicate.getReference().toAddress(),
                new byte[20],
                null,
                null
            ),
            InclusionProofFixture.create(
                SparseMerkleTreePathFixture.create(),
                null,
                unicityCertificate
            )
        ),
        List.of(),
        List.of()
    );
  }

  @Test
  public void testTokenSplitIntoMultipleTokens()
      throws LeafOutOfBoundsException, BranchExistsException {
    Token<?> token = this.createToken(
        new TokenCoinData(
            Map.of(
                new CoinId("coin1".getBytes()),
                BigInteger.valueOf(100)
            )
        ));

    Predicate predicate = new MaskedPredicate(
        token.getId(),
        token.getType(),
        new byte[32],
        "secp256k1",
        HashAlgorithm.SHA256,
        new byte[32]
    );

    TokenSplitBuilder builder = new TokenSplitBuilder();

    Assertions.assertThrows(IllegalArgumentException.class,
        () -> builder.createToken(
            new TokenId(UUID.randomUUID().toString().getBytes()),
            token.getType(),
            null,
            new TokenCoinData(Map.of()),
            predicate.getReference().toAddress(),
            new byte[20],
            null
        )
    );

    builder.createToken(
        new TokenId(UUID.randomUUID().toString().getBytes()),
        token.getType(),
        null,
        new TokenCoinData(
            Map.of(
                new CoinId("coin1".getBytes()),
                BigInteger.valueOf(50)
            )
        ),
        predicate.getReference().toAddress(),
        new byte[20],
        null
    );

    Exception exception = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> builder.build(token)
    );

    Assertions.assertEquals("Token contained 100 CoinId{bytes=636f696e31} coins, but tree has 50",
        exception.getMessage());

    builder.createToken(
        new TokenId(UUID.randomUUID().toString().getBytes()),
        token.getType(),
        null,
        new TokenCoinData(
            Map.of(
                new CoinId("coin1".getBytes()),
                BigInteger.valueOf(50)
            )
        ),
        predicate.getReference().toAddress(),
        new byte[20],
        null
    );

    builder.build(token);
  }

  @Test
  public void testTokenSplitUnknownSplitCoin() {
    Token<?> token = this.createToken(null);

    Predicate predicate = new MaskedPredicate(
        token.getId(),
        token.getType(),
        new byte[32],
        "secp256k1",
        HashAlgorithm.SHA256,
        new byte[32]
    );

    Exception exception = Assertions.assertThrows(
        IllegalArgumentException.class, () -> {
          TokenSplitBuilder builder = new TokenSplitBuilder();
          builder
              .createToken(
                  new TokenId(UUID.randomUUID().toString().getBytes()),
                  token.getType(),
                  null,
                  new TokenCoinData(
                      Map.of(
                          new CoinId("coin1".getBytes()),
                          BigInteger.valueOf(100)
                      )
                  ),
                  predicate.getReference().toAddress(),
                  new byte[20],
                  null
              )
              .build(token);
        });
    Assertions.assertEquals(
        "Token has different number of coins than expected",
        exception.getMessage()
    );
  }
}
