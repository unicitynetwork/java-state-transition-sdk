
package com.unicity.sdk.predicate;

import com.unicity.sdk.ISerializable;

public interface IPredicateFactory {
    IPredicate create(ISerializable data);
}
