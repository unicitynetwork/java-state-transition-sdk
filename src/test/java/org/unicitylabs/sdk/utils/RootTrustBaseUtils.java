package org.unicitylabs.sdk.utils;

import java.util.Map;
import java.util.Set;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.bft.UnicityCertificate;

public class RootTrustBaseUtils {
  public static RootTrustBase generateRootTrustBase(byte[] publicKey) {
    return new RootTrustBase(
        0,
        0,
        0,
        0,
        Set.of(
            new RootTrustBase.NodeInfo(
                "NODE",
                publicKey,
                1
            )
        ),
        1,
        new byte[0],
        new byte[0],
        null,
        Map.of()
    );
  }
}
