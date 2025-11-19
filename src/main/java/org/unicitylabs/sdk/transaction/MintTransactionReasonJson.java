package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import org.unicitylabs.sdk.transaction.split.SplitMintReason;

/**
 * Mint transaction reason deserializer implementation.
 */
public class MintTransactionReasonJson {

  private MintTransactionReasonJson() {
  }

  /**
   * Sparse merkle tree path step deserializer.
   */
  public static class Deserializer extends StdDeserializer<MintTransactionReason> {

    /**
     * Create deserializer.
     */
    public Deserializer() {
      super(MintTransactionReason.class);
    }

    /**
     * Deserialize mint transaction reason.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization
     *            activity.
     * @return mint transaction reason
     * @throws IOException on deserialization failure
     */
    @Override
    public MintTransactionReason deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      return p.readValueAs(SplitMintReason.class);
    }
  }
}

