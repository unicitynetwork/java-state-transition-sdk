
package com.unicity.sdk.shared.signing;

public interface ISigningService {
    ISignature sign(byte[] data);
    boolean verify(byte[] data, ISignature signature);
    byte[] getPublicKey();
}
