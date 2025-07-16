package com.unicity.sdk.predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.unicity.sdk.address.IAddress;

public interface IPredicateReference {
    IAddress toAddress();
}
