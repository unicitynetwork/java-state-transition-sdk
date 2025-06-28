package com.unicity.sdk.shared.hash;

public interface IDataHasherFactory<T extends IDataHasher> {
    /**
     * The hash algorithm used by the data hasher.
     */
    HashAlgorithm getAlgorithm();
    
    /**
     * Creates a new instance of the data hasher.
     * @return IDataHasher instance.
     */
    T create();
}