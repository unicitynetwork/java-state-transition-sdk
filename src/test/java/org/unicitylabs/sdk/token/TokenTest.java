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
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.predicate.embedded.MaskedPredicate;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.InclusionProof;
import org.unicitylabs.sdk.transaction.MintTransactionData;
import org.unicitylabs.sdk.transaction.NametagMintTransactionData;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.utils.TestUtils;
import org.unicitylabs.sdk.utils.UnicityCertificateUtils;
import org.unicitylabs.sdk.verification.VerificationException;

public class TokenTest {

  @Test
  public void testJsonSerialization() throws IOException, VerificationException {
    SigningService signingService = new SigningService(SigningService.generatePrivateKey());
    UnicityCertificate unicityCertificate = UnicityCertificateUtils.generateCertificate(
        signingService, DataHash.fromImprint(new byte[34]));

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
    MintTransactionData<?> nametagGenesisData = new NametagMintTransactionData<>(
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
        new Transaction<>(
            nametagGenesisData,
            new InclusionProof(
                new SparseMerkleTreePath(
                    new DataHash(HashAlgorithm.SHA256, TestUtils.randomBytes(32)),
                    List.of()
                ),
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
        new Transaction<>(
            genesisData,
            new InclusionProof(
                new SparseMerkleTreePath(
                    new DataHash(HashAlgorithm.SHA256, TestUtils.randomBytes(32)),
                    List.of()
                ),
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
