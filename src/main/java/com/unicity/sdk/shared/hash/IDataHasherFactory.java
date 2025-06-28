package com.unicity.sdk.shared.hash;

public interface IDataHasherFactory {
    IDataHasher create(HashAlgorithm algorithm);
}