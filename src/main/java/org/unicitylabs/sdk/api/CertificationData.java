package org.unicitylabs.sdk.api;

import org.unicitylabs.sdk.crypto.MintSigningService;
import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.crypto.secp256k1.SigningService;
import org.unicitylabs.sdk.predicate.EncodedPredicate;
import org.unicitylabs.sdk.predicate.UnlockScript;
import org.unicitylabs.sdk.predicate.builtin.SignaturePredicateUnlockScript;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.util.HexConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Certification data.
 */
public class CertificationData {
  public static final long CBOR_TAG = 39031;
  /** The only accepted wire version. One version, one element count. */
  public static final int VERSION = 2;
  private static final int FIELD_COUNT = 6;

  private final EncodedPredicate lockScript;
  private final DataHash sourceStateHash;
  private final DataHash transactionHash;
  private final Long expiresAt;
  private final byte[] unlockScript;

  CertificationData(
          EncodedPredicate lockScript,
          DataHash sourceStateHash,
          DataHash transactionHash,
          Long expiresAt,
          byte[] unlockScript
  ) {
    this.lockScript = lockScript;
    this.sourceStateHash = sourceStateHash;
    this.transactionHash = transactionHash;
    this.expiresAt = expiresAt;
    this.unlockScript = Arrays.copyOf(unlockScript, unlockScript.length);
  }

  public int getVersion() {
    return VERSION;
  }

  /**
   * Get lock script of certified transaction output.
   *
   * @return lock script
   */
  public EncodedPredicate getLockScript() {
    return this.lockScript;
  }

  /**
   * Get source state hash.
   *
   * @return source state hash
   */
  public DataHash getSourceStateHash() {
    return this.sourceStateHash;
  }

  /**
   * Get transaction hash.
   *
   * @return transaction hash
   */
  public DataHash getTransactionHash() {
    return this.transactionHash;
  }

  /**
   * Get the exclusive certification request deadline in Unix seconds.
   *
   * @return request deadline, empty when the Unicity Service assigns one
   */
  public Optional<Long> getExpiresAt() {
    return Optional.ofNullable(this.expiresAt);
  }

  /**
   * Get unlock script used for certification.
   *
   * @return unlock script bytes
   */
  public byte[] getUnlockScript() {
    return Arrays.copyOf(this.unlockScript, this.unlockScript.length);
  }

  /**
   * Deserialize CertificationData from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return CertificationData
   */
  public static CertificationData fromCbor(byte[] bytes) {
    CborDeserializer.CborTag tag = CborDeserializer.decodeTag(bytes);
    if (tag.getTag() != CertificationData.CBOR_TAG) {
      throw new CborSerializationException(String.format("Invalid CBOR tag: %s", tag.getTag()));
    }
    List<byte[]> data = CborDeserializer.decodeArray(tag.getData(), FIELD_COUNT);

    int version = CborDeserializer.decodeUnsignedInteger(data.get(0)).asInt();
    if (version != VERSION) {
      throw new CborSerializationException(String.format("Unsupported version: %s", version));
    }

    return new CertificationData(
            EncodedPredicate.fromCbor(data.get(1)),
            new DataHash(HashAlgorithm.SHA256, CborDeserializer.decodeByteString(data.get(2))),
            new DataHash(HashAlgorithm.SHA256, CborDeserializer.decodeByteString(data.get(3))),
            CborDeserializer.decodeNullable(
                    data.get(4), value -> CborDeserializer.decodeUnsignedInteger(value).asLong()),
            CborDeserializer.decodeByteString(data.get(5))
    );
  }

  /**
   * Build certification data for a mint transaction using the deterministic mint signing service.
   *
   * @param transaction mint transaction
   *
   * @return certification data
   */
  public static CertificationData fromMintTransaction(MintTransaction transaction) {
    Objects.requireNonNull(transaction, "transaction cannot be null");

    SigningService signingService = MintSigningService.create(transaction.getTokenId());

    return CertificationData.fromTransaction(
            transaction,
            SignaturePredicateUnlockScript.create(transaction, signingService).getSignature()
                    .encode()
    );
  }

  /**
   * Build certification data from a transaction and unlock script object.
   *
   * @param transaction transaction to certify
   * @param unlockScript unlock script
   *
   * @return certification data
   */
  public static CertificationData fromTransaction(Transaction transaction, UnlockScript unlockScript) {
    Objects.requireNonNull(unlockScript, "unlockScript cannot be null");

    return CertificationData.fromTransaction(transaction, unlockScript.encode());
  }

  /**
   * Build certification data from a transaction and encoded unlock script bytes.
   *
   * @param transaction transaction to certify
   * @param unlockScript encoded unlock script bytes
   *
   * @return certification data
   */
  public static CertificationData fromTransaction(Transaction transaction, byte[] unlockScript) {
    Objects.requireNonNull(transaction, "transaction cannot be null");
    Objects.requireNonNull(unlockScript, "unlockScript cannot be null");

    return new CertificationData(
            transaction.getLockScript(),
            transaction.getSourceStateHash(),
            transaction.calculateTransactionHash(),
            transaction.getExpiresAt().orElse(null),
            unlockScript
    );
  }

  /**
   * Serialize certification data to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeTag(
            CertificationData.CBOR_TAG,
            CborSerializer.encodeArray(
                    CborSerializer.encodeUnsignedInteger(VERSION),
                    this.lockScript.toCbor(),
                    CborSerializer.encodeByteString(this.sourceStateHash.getData()),
                    CborSerializer.encodeByteString(this.transactionHash.getData()),
                    CborSerializer.encodeNullable(this.expiresAt, CborSerializer::encodeUnsignedInteger),
                    CborSerializer.encodeByteString(this.unlockScript))
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CertificationData)) {
      return false;
    }
    CertificationData that = (CertificationData) o;
    return Objects.equals(this.lockScript, that.lockScript)
            && Objects.equals(this.sourceStateHash, that.sourceStateHash)
            && Objects.equals(this.transactionHash, that.transactionHash)
            && Objects.equals(this.expiresAt, that.expiresAt)
            && Arrays.equals(this.unlockScript, that.unlockScript);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.lockScript, this.sourceStateHash, this.transactionHash, this.expiresAt,
            Arrays.hashCode(this.unlockScript));
  }

  @Override
  public String toString() {
    return String.format(
            "CertificationData{lockScript=%s, sourceStateHash=%s, transactionHash=%s, expiresAt=%s, unlockScript=%s}",
            this.lockScript, this.sourceStateHash, this.transactionHash, this.expiresAt,
            HexConverter.encode(this.unlockScript));
  }
}
