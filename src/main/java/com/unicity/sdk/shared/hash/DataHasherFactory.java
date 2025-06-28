package com.unicity.sdk.shared.hash;

import java.util.function.Supplier;

public class DataHasherFactory<T extends IDataHasher> implements IDataHasherFactory<T> {
    private final HashAlgorithm algorithm;
    private final Supplier<T> hasherConstructor;
    
    public DataHasherFactory(HashAlgorithm algorithm, Supplier<T> hasherConstructor) {
        this.algorithm = algorithm;
        this.hasherConstructor = hasherConstructor;
    }
    
    @Override
    public HashAlgorithm getAlgorithm() {
        return algorithm;
    }
    
    @Override
    public T create() {
        return hasherConstructor.get();
    }
}