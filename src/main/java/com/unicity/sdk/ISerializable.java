
package com.unicity.sdk;

/**
 * Interface for serializable objects that can be converted to CBOR and JSON.
 */
public interface ISerializable {
    /**
     * Serialize the object into a JSON friendly structure.
     *
     * @return Serializable JSON value
     */
    Object toJSON();

    /**
     * Serialize the object into a CBOR byte array.
     *
     * @return CBOR encoded representation of the object
     */
    byte[] toCBOR();
}
