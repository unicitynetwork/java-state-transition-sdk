package org.unicitylabs.sdk.api.bft;

import org.unicitylabs.sdk.api.NetworkId;

import java.util.List;
import java.util.Map;

public class RootTrustBaseUtils {
  public static RootTrustBase generateRootTrustBase(byte[] publicKey) {
    return new RootTrustBase(
            1,
            NetworkId.LOCAL,
            0L,
            0L,
            List.of(
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
