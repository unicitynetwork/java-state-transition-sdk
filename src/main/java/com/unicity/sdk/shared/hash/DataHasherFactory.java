package com.unicity.sdk.shared.hash;

public class DataHasherFactory implements IDataHasherFactory {
    @Override
    public IDataHasher create(HashAlgorithm algorithm) {
        return new IDataHasher() {
            @Override
            public DataHash digest(byte[] data) {
                return DataHasher.digest(algorithm, data);
            }
        };
    }
}