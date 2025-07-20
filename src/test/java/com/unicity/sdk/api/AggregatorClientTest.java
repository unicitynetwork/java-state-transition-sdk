package com.unicity.sdk.api;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.HashAlgorithm;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

public class AggregatorClientTest {

  @Test
  public void testSubmitCommitment() throws ExecutionException, InterruptedException {
//    Commitment.create(MintTransactionData)
//    System.out.println(
//        new AggregatorClient("https://gateway-test.unicity.network/").submitCommitment();
  }

  @Test
  public void testGetInclusionProof() throws ExecutionException, InterruptedException {
    System.out.println(
        new AggregatorClient("https://gateway-test.unicity.network/").getInclusionProof(
            RequestId.create(new byte[32], new DataHash(
                HashAlgorithm.SHA256, new byte[32]))).get());
  }

  @Test
  public void testBlockHeight() throws ExecutionException, InterruptedException {
    System.out.println(
        new AggregatorClient("https://gateway-test.unicity.network/").getBlockHeight().get());
  }

}
