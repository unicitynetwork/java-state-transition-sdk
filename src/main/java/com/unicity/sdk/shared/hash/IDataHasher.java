package com.unicity.sdk.shared.hash;

import java.util.concurrent.CompletableFuture;

public interface IDataHasher {
    HashAlgorithm getAlgorithm();
    
    IDataHasher update(byte[] data);
    
    CompletableFuture<DataHash> digest();
}