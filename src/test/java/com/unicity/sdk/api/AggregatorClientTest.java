package com.unicity.sdk.api;

import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

public class AggregatorClientTest {

  @Test
  public void testSubmitCommitment() throws ExecutionException, InterruptedException {
    System.out.println(new AggregatorClient("https://gateway-test.unicity.network/").getBlockHeight().get());
  }

}
