package org.unicitylabs.sdk.token;

import org.unicitylabs.sdk.address.DirectAddress;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.predicate.MaskedPredicate;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.InclusionProof;
import org.unicitylabs.sdk.transaction.MintTransactionData;
import org.unicitylabs.sdk.transaction.NametagMintTransactionData;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferTransactionData;
import org.unicitylabs.sdk.utils.TestUtils;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TokenTest {

  @Test
  public void testJsonSerialization() throws IOException {
    MintTransactionData<?> genesisData = new MintTransactionData<>(
        new TokenId(TestUtils.randomBytes(32)),
        new TokenType(TestUtils.randomBytes(32)),
        TestUtils.randomBytes(10),
        new TokenCoinData(Map.of(
            new CoinId(TestUtils.randomBytes(10)), BigInteger.valueOf(100),
            new CoinId(TestUtils.randomBytes(4)), BigInteger.valueOf(3))),
        DirectAddress.create(new DataHash(HashAlgorithm.SHA256, TestUtils.randomBytes(32))),
        TestUtils.randomBytes(32),
        null,
        null
    );

    byte[] nametagNonce = TestUtils.randomBytes(32);
    NameTagTokenState nametagTokenState = new NameTagTokenState(
        MaskedPredicate.create(
            SigningService.createFromSecret(TestUtils.randomBytes(32), nametagNonce),
            HashAlgorithm.SHA256,
            nametagNonce),
        DirectAddress.create(new DataHash(HashAlgorithm.SHA256, TestUtils.randomBytes(32)))
    );
    MintTransactionData<?> nametagGenesisData = new NametagMintTransactionData<>(
        UUID.randomUUID().toString(),
        new TokenType(TestUtils.randomBytes(32)),
        TestUtils.randomBytes(5),
        null,
        DirectAddress.create(new DataHash(HashAlgorithm.SHA256, TestUtils.randomBytes(32))),
        TestUtils.randomBytes(32),
        nametagTokenState.getAddress()
    );

    Token<?> nametagToken = new Token<>(
        nametagTokenState,
        new Transaction<>(
            nametagGenesisData,
            new InclusionProof(
                new SparseMerkleTreePath(
                    new DataHash(HashAlgorithm.SHA256, TestUtils.randomBytes(32)),
                    List.of()
                ),
                null,
                null
            )
        )
    );

    Token<?> token = new Token<>(
        new TokenState(
            MaskedPredicate.create(
                SigningService.createFromSecret(TestUtils.randomBytes(32),
                    genesisData.getTokenId().getBytes()),
                HashAlgorithm.SHA256,
                TestUtils.randomBytes(24)),
            null
        ),
        new Transaction<>(
            genesisData,
            new InclusionProof(
                new SparseMerkleTreePath(
                    new DataHash(HashAlgorithm.SHA256, TestUtils.randomBytes(32)),
                    List.of()
                ),
                null,
                null
            )
        ),
        List.of(
            new Transaction<>(
                new TransferTransactionData(
                    new TokenState(
                        new MaskedPredicate(
                            new byte[24],
                            "secp256k1",
                            HashAlgorithm.SHA256,
                            new byte[25]
                        ),
                        null
                    ),
                    DirectAddress.create(new DataHash(HashAlgorithm.SHA256, new byte[32])),
                    new byte[20],
                    null,
                    "Transfer".getBytes(),
                    List.of(nametagToken)
                ),
                new InclusionProof(
                    new SparseMerkleTreePath(
                        new DataHash(HashAlgorithm.SHA256, TestUtils.randomBytes(32)),
                        List.of()
                    ),
                    null,
                    null
                )
            )
        ),
        List.of(nametagToken)
    );

    Assertions.assertEquals(token,
        UnicityObjectMapper.JSON.readValue(
            UnicityObjectMapper.JSON.writeValueAsString(token),
            Token.class));
  }

}
