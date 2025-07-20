package com.unicity.sdk.transaction;

import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.predicate.MaskedPredicateReference;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.CoinId;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.util.HexConverter;
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
        TokenCoinData.create(Map.of(
            new CoinId(new byte[10]), BigInteger.ONE,
            new CoinId(new byte[5]), BigInteger.TEN
        )),
        predicateReference.toAddress(),
        new byte[10],
        new DataHash(HashAlgorithm.SHA256, new byte[32]),
        null
    );
    Commitment<MintTransactionData<MintTransactionReason>> commitment = new Commitment<>(
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
            UnicityObjectMapper.JSON.getTypeFactory()
                .constructParametricType(Commitment.class, MintTransactionData.class)));
  }

}
