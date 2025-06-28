package com.unicity.sdk.shared.hash;

public interface IDataHasher {
    DataHash digest(byte[] data);
}