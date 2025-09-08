package org.unicitylabs.sdk.transaction.split;

import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.BranchExistsException;
import org.unicitylabs.sdk.mtree.LeafOutOfBoundsException;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.predicate.MaskedPredicate;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.InclusionProof;
import org.unicitylabs.sdk.transaction.MintTransactionData;
import org.unicitylabs.sdk.transaction.Transaction;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TokenSplitBuilderTest {

  private Token<?> createToken(TokenCoinData coinData) {
    Predicate predicate = new MaskedPredicate(
        new byte[32],
        "secp256k1",
        HashAlgorithm.SHA256,
        new byte[32]
    );

    TokenId tokenId = new TokenId(new byte[10]);
    TokenType tokenType = new TokenType(new byte[10]);

    return new Token<>(
        new TokenState(predicate, null),
        new Transaction<>(
            new MintTransactionData<>(
                tokenId,
                tokenType,
                null,
                coinData,
                predicate.getReference(tokenType).toAddress(),
                new byte[20],
                null,
                null
            ),
            new InclusionProof(
                new SparseMerkleTreePath(
                    new DataHash(HashAlgorithm.SHA256, new byte[32]),
                    List.of()
                ),
                null,
                null
            )
        )
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
        new byte[32],
        "secp256k1",
        HashAlgorithm.SHA256,
        new byte[32]
    );

    TokenSplitBuilder builder = new TokenSplitBuilder();

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      builder.createToken(
          new TokenId(UUID.randomUUID().toString().getBytes()),
          token.getType(),
          null,
          new TokenCoinData(Map.of()),
          predicate.getReference(token.getType()).toAddress(),
          new byte[20],
          null
      );
    });

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
        predicate.getReference(token.getType()).toAddress(),
        new byte[20],
        null
    );

    Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
      builder.build(token);
    });

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
        predicate.getReference(token.getType()).toAddress(),
        new byte[20],
        null
    );

    builder.build(token);
  }

  @Test
  public void testTokenSplitUnknownSplitCoin() {
    Predicate predicate = new MaskedPredicate(
        new byte[32],
        "secp256k1",
        HashAlgorithm.SHA256,
        new byte[32]
    );

    Token<?> token = this.createToken(null);

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
                  predicate.getReference(token.getType()).toAddress(),
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
