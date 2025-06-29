
package com.unicity.sdk.shared.signing;

import com.unicity.sdk.ISerializable;

public interface ISignature extends ISerializable {
    byte[] getBytes();
}
