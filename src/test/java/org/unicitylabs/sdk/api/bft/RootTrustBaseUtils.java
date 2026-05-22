package org.unicitylabs.sdk.api.bft;

import org.unicitylabs.sdk.api.NetworkId;

import java.util.Map;
import java.util.Set;

public class RootTrustBaseUtils {
  public static RootTrustBase generateRootTrustBase(byte[] publicKey) {
    return new RootTrustBase(
            0,
            NetworkId.LOCAL,
            0L,
            0L,
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
