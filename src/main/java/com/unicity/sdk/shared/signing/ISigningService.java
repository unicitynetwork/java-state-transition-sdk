
package com.unicity.sdk.shared.signing;

import com.unicity.sdk.shared.hash.DataHash;
import java.util.concurrent.CompletableFuture;

public interface ISigningService<T extends ISignature> {
    byte[] getPublicKey();
    String getAlgorithm();
    CompletableFuture<T> sign(DataHash hash);
    CompletableFuture<Boolean> verify(DataHash hash, T signature);
}
