package org.unicitylabs.sdk.payment;

import org.unicitylabs.sdk.crypto.hash.DataHasher;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.EncodedPredicate;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.transaction.Token;
import org.unicitylabs.sdk.transaction.TokenId;
import org.unicitylabs.sdk.transaction.TokenSalt;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Split mint reason: CBOR semantic tag 39044 applied to the two-element array
 * {@code [burned source token, split allocation proofs]}, where the proofs appear in canonical
 * output-asset order, one per asset the minted output carries.
 */
public final class SplitMintJustification {
  public static final long CBOR_TAG = 39044;

  /** ASCII domain separator {@code UNICITY_SPLIT_OUTPUT} for the split output commitment. */
  private static final byte[] SPLIT_OUTPUT =
          "UNICITY_SPLIT_OUTPUT".getBytes(StandardCharsets.US_ASCII);

  private final Token token;
  private final List<SplitAllocationProof> proofs;

  private SplitMintJustification(Token token, List<SplitAllocationProof> proofs) {
    this.token = token;
    this.proofs = List.copyOf(proofs);
  }

  /**
   * Get the burn token whose split produced this justification.
   *
   * @return burn token
   */
  public Token getToken() {
    return this.token;
  }

  /**
   * Get the allocation proofs supporting this split mint justification.
   *
   * @return proofs
   */
  public List<SplitAllocationProof> getProofs() {
    return this.proofs;
  }

  /**
   * Calculate the sum-tree leaf data {@code d_j} for a split output: a commitment that binds an
   * allocation leaf to its output mint transaction. Every term is a CBOR byte string except the
   * network identifier, which is an unsigned integer. The mint reason is deliberately excluded —
   * it embeds the proofs, which are derived from this value.
   *
   * @param token token which is going to be burnt; its identifier, network and token type are
   *     bound into the commitment (a split preserves the source network and token type, so the
   *     output's equal them)
   * @param recipient output recipient predicate
   * @param salt output mint salt
   * @param tokenId output token identifier
   * @param data exact output auxiliary-payload byte string, or {@code null}
   * @return raw 32-byte commitment digest
   */
  public static byte[] calculateLeafData(
          Token token,
          EncodedPredicate recipient,
          TokenSalt salt,
          TokenId tokenId,
          byte[] data
  ) {
    return new DataHasher(HashAlgorithm.SHA256)
            .update(
                    CborSerializer.encodeArray(
                            CborSerializer.encodeByteString(SplitMintJustification.SPLIT_OUTPUT),
                            CborSerializer.encodeByteString(token.getId().getBytes()),
                            CborSerializer.encodeUnsignedInteger(
                                    token.getGenesis().getNetworkId().getId()),
                            CborSerializer.encodeByteString(recipient.toCbor()),
                            salt.toCbor(),
                            tokenId.toCbor(),
                            token.getType().toCbor(),
                            CborSerializer.encodeNullable(data, CborSerializer::encodeByteString)
                    )
            )
            .digest()
            .getData();
  }

  /**
   * Create a SplitMintJustification.
   *
   * @param token burned source token (including its certified burn transfer)
   * @param proofs allocation proofs in canonical output-asset order
   * @return new justification
   * @throws IllegalArgumentException if {@code proofs} is empty
   */
  public static SplitMintJustification create(Token token, List<SplitAllocationProof> proofs) {
    Objects.requireNonNull(token, "token cannot be null");
    Objects.requireNonNull(proofs, "proofs cannot be null");

    if (proofs.isEmpty()) {
      throw new IllegalArgumentException("proofs cannot be empty.");
    }

    return new SplitMintJustification(token, proofs);
  }

  /**
   * Create SplitMintJustification from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return decoded justification
   * @throws CborSerializationException on wrong tag
   */
  public static SplitMintJustification fromCbor(byte[] bytes) {
    CborDeserializer.CborTag tag = CborDeserializer.decodeTag(bytes);
    if (tag.getTag() != SplitMintJustification.CBOR_TAG) {
      throw new CborSerializationException(
              String.format("Invalid CBOR tag for SplitMintJustification: %s", tag.getTag()));
    }

    List<byte[]> data = CborDeserializer.decodeArray(tag.getData(), 2);

    return SplitMintJustification.create(
            Token.fromCbor(data.get(0)),
            CborDeserializer.decodeArray(data.get(1)).stream()
                    .map(SplitAllocationProof::fromCbor)
                    .collect(Collectors.toList())
    );
  }

  /**
   * Convert SplitMintJustification to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeTag(
            SplitMintJustification.CBOR_TAG,
            CborSerializer.encodeArray(
                    this.token.toCbor(),
                    CborSerializer.encodeArray(
                            this.proofs.stream()
                                    .map(SplitAllocationProof::toCbor)
                                    .toArray(byte[][]::new))
            )
    );
  }
}
