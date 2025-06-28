
package com.unicity.sdk.serializer.token;

import com.unicity.sdk.token.Token;

public interface ITokenDeserializer {
    Token deserialize(Object data);
}
