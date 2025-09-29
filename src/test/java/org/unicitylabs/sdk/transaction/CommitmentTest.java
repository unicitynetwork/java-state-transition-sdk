package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.embedded.MaskedPredicateReference;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.util.HexConverter;

public class CommitmentTest {

  @Test
  public void testJsonSerialization() throws IOException {
    SigningService signingService = new SigningService(
        HexConverter.decode("0000000000000000000000000000000000000000000000000000000000000001"));
    TokenType tokenType = new TokenType(new byte[32]);
    byte[] nonce = new byte[64];

    MaskedPredicateReference predicateReference = MaskedPredicateReference.create(tokenType,
        signingService.getAlgorithm(), signingService.getPublicKey(), HashAlgorithm.SHA256, nonce);
    MintTransaction.Data<MintTransactionReason> transactionData = new MintTransaction.Data<>(
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
    MintCommitment<?> commitment = MintCommitment.create(transactionData);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new Jdk8Module());

    Assertions.assertEquals(commitment,
        mapper.readValue(
            mapper.writeValueAsString(commitment),
            MintCommitment.class
        )
    );
  }

}
