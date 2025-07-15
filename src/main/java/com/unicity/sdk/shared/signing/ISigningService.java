
package com.unicity.sdk.shared.signing;

import com.unicity.sdk.shared.hash.DataHash;
import java.util.concurrent.CompletableFuture;

public interface ISigningService<T extends ISignature> {
    byte[] getPublicKey();
    String getAlgorithm();
    T sign(DataHash hash);
    boolean verify(DataHash hash, T signature);
}
