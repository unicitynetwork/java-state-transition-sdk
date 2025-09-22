package org.unicitylabs.sdk.utils;

import java.util.List;
import java.util.Map;
import org.unicitylabs.sdk.bft.InputRecord;
import org.unicitylabs.sdk.bft.ShardTreeCertificate;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.bft.UnicitySeal;
import org.unicitylabs.sdk.bft.UnicityTreeCertificate;

public class UnicityCertificateUtils {
  public static UnicityCertificate generateCertificate() {
    return new UnicityCertificate(
        0,
        new InputRecord(0, 0, 0, new byte[10], new byte[10], new byte[10], 0,
            new byte[10], 0, new byte[10]),
        new byte[10],
        new byte[10],
        new ShardTreeCertificate(new byte[10], List.of()),
        new UnicityTreeCertificate(0, 0, List.of()),
        new UnicitySeal(0, (short) 0, 0L, 0L, 0L, new byte[10], new byte[10], Map.of())
    );
  }
}
