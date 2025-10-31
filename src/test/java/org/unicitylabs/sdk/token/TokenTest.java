package org.unicitylabs.sdk.token;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.address.DirectAddress;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.bft.UnicityCertificateUtils;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePathFixture;
import org.unicitylabs.sdk.predicate.embedded.MaskedPredicate;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.InclusionProofFixture;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.transaction.MintTransactionFixture;
import org.unicitylabs.sdk.utils.TestUtils;
import org.unicitylabs.sdk.verification.VerificationException;

public class TokenTest {

  @Test
  public void testJsonSerialization() throws IOException, VerificationException {
    SigningService signingService = new SigningService(SigningService.generatePrivateKey());
    UnicityCertificate unicityCertificate = UnicityCertificateUtils.generateCertificate(
        signingService, DataHash.fromImprint(new byte[34]));

    MintTransaction.Data<?> genesisData = new MintTransaction.Data<>(
        new TokenId(TestUtils.randomBytes(32)),
        new TokenType(TestUtils.randomBytes(32)),
        TestUtils.randomBytes(10),
        new TokenCoinData(
            Map.of(
                new CoinId(TestUtils.randomBytes(10)), BigInteger.valueOf(100),
                new CoinId(TestUtils.randomBytes(4)), BigInteger.valueOf(3)
            )
        ),
        DirectAddress.create(new DataHash(HashAlgorithm.SHA256, TestUtils.randomBytes(32))),
        TestUtils.randomBytes(32),
        null,
        null
    );

    byte[] nametagNonce = TestUtils.randomBytes(32);
    MintTransaction.NametagData nametagGenesisData = new MintTransaction.NametagData(
        UUID.randomUUID().toString(),
        new TokenType(TestUtils.randomBytes(32)),
        DirectAddress.create(new DataHash(HashAlgorithm.SHA256, TestUtils.randomBytes(32))),
        TestUtils.randomBytes(32),
        DirectAddress.create(new DataHash(HashAlgorithm.SHA256, TestUtils.randomBytes(32)))
    );

    Token<?> nametagToken = new Token<>(
        new TokenState(
            MaskedPredicate.create(
                nametagGenesisData.getTokenId(),
                nametagGenesisData.getTokenType(),
                SigningService.createFromMaskedSecret(TestUtils.randomBytes(32), nametagNonce),
                HashAlgorithm.SHA256,
                nametagNonce),
            null),
        MintTransactionFixture.create(
            nametagGenesisData,
            InclusionProofFixture.create(
                SparseMerkleTreePathFixture.create(),
                null,
                null,
                unicityCertificate
            )
        ),
        List.of(),
        List.of()
    );

    Token<?> token = new Token<>(
        new TokenState(
            MaskedPredicate.create(
                genesisData.getTokenId(),
                genesisData.getTokenType(),
                SigningService.createFromMaskedSecret(
                    TestUtils.randomBytes(32),
                    genesisData.getTokenId().getBytes()
                ),
                HashAlgorithm.SHA256,
                TestUtils.randomBytes(24)),
            null
        ),
        MintTransactionFixture.create(
            genesisData,
            InclusionProofFixture.create(
                SparseMerkleTreePathFixture.create(),
                null,
                null,
                unicityCertificate
            )
        ),
        List.of(),
        List.of(nametagToken)
    );

    Assertions.assertEquals(token,
        UnicityObjectMapper.JSON.readValue(
            UnicityObjectMapper.JSON.writeValueAsString(token),
            Token.class));
  }

}
