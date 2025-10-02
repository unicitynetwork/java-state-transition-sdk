package org.unicitylabs.sdk.predicate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Predicate structure before passing it to predicate engine.
 */
public class EncodedPredicate implements SerializablePredicate {

  private final PredicateEngineType engine;
  private final byte[] code;
  private final byte[] parameters;


  EncodedPredicate(PredicateEngineType engine, byte[] code, byte[] parameters) {
    Objects.requireNonNull(code, "Code must not be null");
    Objects.requireNonNull(parameters, "Parameters must not be null");

    this.engine = engine;
    this.code = Arrays.copyOf(code, code.length);
    this.parameters = Arrays.copyOf(parameters, parameters.length);
  }

  /**
   * Get predicate engine.
   *
   * @return predicate engine
   */
  public PredicateEngineType getEngine() {
    return this.engine;
  }

  /**
   * Encode predicate code.
   *
   * @return encoded code
   */
  @Override
  public byte[] encode() {
    return Arrays.copyOf(this.code, this.code.length);
  }

  /**
   * Encode predicate parameters.
   *
   * @return encoded parameters
   */
  @Override
  public byte[] encodeParameters() {
    return Arrays.copyOf(this.parameters, this.parameters.length);
  }

  /**
   * Create encoded predicate from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return encoded predicate
   */
  public static EncodedPredicate fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new EncodedPredicate(
        PredicateEngineType.values()[CborDeserializer.readUnsignedInteger(data.get(0)).asInt()],
        CborDeserializer.readByteString(data.get(1)),
        CborDeserializer.readByteString(data.get(2))
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof EncodedPredicate)) {
      return false;
    }
    EncodedPredicate predicate = (EncodedPredicate) o;
    return this.engine == predicate.engine && Objects.deepEquals(this.code, predicate.code)
        && Objects.deepEquals(this.parameters, predicate.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.engine, Arrays.hashCode(this.code), Arrays.hashCode(this.parameters));
  }

  @Override
  public String toString() {
    return String.format("Predicate{engine=%s, code=%s, parameters=%s}", this.engine,
        HexConverter.encode(this.code), HexConverter.encode(this.parameters));
  }
}