package org.unicitylabs.sdk.payment;

import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Split manifest: CBOR semantic tag 39046 applied directly to an array of per-asset sum-tree root
 * hashes, positionally aligned with the source token's assets in canonical order. The certified
 * burn transfer carries the manifest as its auxiliary data, and the burn reason is the SHA-256 of
 * its canonical encoding.
 *
 * <p>Roots are exposed as {@link DataHash} instances, so the digest length is enforced by the
 * hash type. On the wire each root is encoded as its raw digest bytes (no algorithm imprint);
 * decoding reconstructs SHA-256 hashes.
 */
public final class SplitManifest {
  public static final long CBOR_TAG = 39046;

  private final List<DataHash> roots;

  private SplitManifest(List<DataHash> roots) {
    this.roots = List.copyOf(roots);
  }

  /**
   * Get the per-asset RSMST root hashes.
   *
   * @return root hashes
   */
  public List<DataHash> getRoots() {
    return this.roots;
  }

  /**
   * Create a SplitManifest from per-asset root hashes.
   *
   * @param roots RSMST root hashes in canonical source-asset order
   * @return new manifest
   * @throws IllegalArgumentException if {@code roots} is empty
   */
  public static SplitManifest create(List<DataHash> roots) {
    Objects.requireNonNull(roots, "roots cannot be null");

    if (roots.isEmpty()) {
      throw new IllegalArgumentException("Split manifest must contain at least one root.");
    }

    return new SplitManifest(roots);
  }

  /**
   * Create SplitManifest from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return decoded manifest
   * @throws CborSerializationException on wrong tag or malformed roots
   */
  public static SplitManifest fromCbor(byte[] bytes) {
    CborDeserializer.CborTag tag = CborDeserializer.decodeTag(bytes);
    if (tag.getTag() != SplitManifest.CBOR_TAG) {
      throw new CborSerializationException(
              String.format("Invalid CBOR tag for SplitManifest: %s", tag.getTag()));
    }

    List<DataHash> roots = CborDeserializer.decodeArray(tag.getData()).stream()
            .map(root -> {
              byte[] digest = CborDeserializer.decodeByteString(root);
              if (digest.length != HashAlgorithm.SHA256.getLength()) {
                throw new CborSerializationException(
                        "Each split manifest root must be a SHA-256 digest.");
              }

              return new DataHash(HashAlgorithm.SHA256, digest);
            })
            .collect(Collectors.toList());

    return SplitManifest.create(roots);
  }

  /**
   * Convert SplitManifest to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeTag(
            SplitManifest.CBOR_TAG,
            CborSerializer.encodeArray(
                    this.roots.stream()
                            .map(root -> CborSerializer.encodeByteString(root.getData()))
                            .toArray(byte[][]::new))
    );
  }
}
