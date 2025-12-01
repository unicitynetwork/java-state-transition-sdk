package org.unicitylabs.sdk.transaction;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.transaction.split.SplitMintReason;

/**
 * Default implementation for mint reason factory.
 */
public class DefaultMintReasonFactory implements MintReasonFactory {

  private final Map<Long, Function<byte[], MintTransactionReason>> reasons;

  /**
   * Create default mint reason factory.
   *
   * @param reasons mint transaction reason parser map
   */
  public DefaultMintReasonFactory(Map<Long, Function<byte[], MintTransactionReason>> reasons) {
    this.reasons = Map.copyOf(reasons);
  }

  /**
   * Create default mint reason factory.
   */
  public DefaultMintReasonFactory() {
    this(Map.of(SplitMintReason.TYPE, SplitMintReason::fromCbor));
  }

  @Override
  public MintTransactionReason create(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    long type = CborDeserializer.readUnsignedInteger(data.get(0)).asLong();
    Function<byte[], MintTransactionReason> factory = this.reasons.get(type);
    if (factory == null) {
      throw new IllegalStateException(String.format("Unsupported user defined mint reason type '%s'", type));
    }

    return factory.apply(bytes);
  }

}
