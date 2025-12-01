package org.unicitylabs.sdk.token;

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
import org.unicitylabs.sdk.predicate.embedded.MaskedPredicateReference;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.InclusionProofFixture;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.transaction.MintTransaction.Data;
import org.unicitylabs.sdk.transaction.MintTransactionFixture;
import org.unicitylabs.sdk.transaction.split.TokenSplitBuilder;

public class TokenSplitBuilderTest {

  private Token createToken(TokenCoinData coinData) {
    UnicityCertificate unicityCertificate = UnicityCertificateUtils.generateCertificate(
        new SigningService(SigningService.generatePrivateKey()), DataHash.fromImprint(new byte[34]));

    TokenId tokenId = new TokenId(new byte[10]);
    TokenType tokenType = new TokenType(new byte[10]);
    byte[] nonce = new byte[32];

    SigningService signingService = SigningService.createFromMaskedSecret("SECRET".getBytes(), nonce);

    MintTransaction transaction = MintTransactionFixture.create(
        new Data(
            tokenId,
            tokenType,
            null,
            coinData,
            MaskedPredicateReference.create(tokenType, signingService, HashAlgorithm.SHA256, nonce).toAddress(),
            new byte[20]
        ),
        InclusionProofFixture.create(
            SparseMerkleTreePathFixture.create(),
            null,
            unicityCertificate
        )
    );

    Predicate predicate = MaskedPredicate.create(
        tokenId,
        tokenType,
        signingService,
        HashAlgorithm.SHA256,
        nonce
    );

    return new Token(
        new TokenState(predicate, null),
        transaction,
        List.of(),
        List.of()
    );
  }

  @Test
  public void testTokenSplitIntoMultipleTokens()
      throws LeafOutOfBoundsException, BranchExistsException {
    Token token = this.createToken(
        new TokenCoinData(
            Map.of(
                new CoinId("coin1".getBytes()),
                BigInteger.valueOf(100)
            )
        ));

    Predicate predicate = MaskedPredicate.create(
        token.getId(),
        token.getType(),
        SigningService.createFromSecret(SigningService.generatePrivateKey()),
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
    Token token = this.createToken(null);

    Predicate predicate = MaskedPredicate.create(
        token.getId(),
        token.getType(),
        SigningService.createFromSecret(SigningService.generatePrivateKey()),
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
