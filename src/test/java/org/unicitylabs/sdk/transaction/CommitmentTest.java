package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.MaskedPredicateReference;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.util.HexConverter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommitmentTest {

  @Test
  public void testJsonSerialization() throws IOException {
    SigningService signingService = new SigningService(
        HexConverter.decode("0000000000000000000000000000000000000000000000000000000000000001"));
    TokenType tokenType = new TokenType(new byte[32]);
    byte[] nonce = new byte[64];

    MaskedPredicateReference predicateReference = MaskedPredicateReference.create(tokenType,
        signingService.getAlgorithm(), signingService.getPublicKey(), HashAlgorithm.SHA256, nonce);
    MintTransactionData<MintTransactionReason> transactionData = new MintTransactionData<>(
        new TokenId(new byte[32]),
        tokenType,
        new byte[5],
        new TokenCoinData(Map.of(
            new CoinId(new byte[10]), BigInteger.ONE,
            new CoinId(new byte[5]), BigInteger.TEN
        )),
        predicateReference.toAddress(),
        new byte[10],
        new DataHash(HashAlgorithm.SHA256, new byte[32]),
        null
    );
    Commitment<MintTransactionData<MintTransactionReason>> commitment = new MintCommitment<>(
        new RequestId(
            new DataHash(HashAlgorithm.SHA256, new byte[32])
        ),
        transactionData,
        Authenticator.create(
            signingService,
            transactionData.calculateHash(),
            transactionData.getSourceState().getHash())
    );

    Assertions.assertEquals(commitment,
        UnicityObjectMapper.JSON.readValue(
            UnicityObjectMapper.JSON.writeValueAsString(commitment),
            MintCommitment.class
        )
    );
  }

}
