package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.api.InclusionProof;
import org.unicitylabs.sdk.api.NetworkId;
import org.unicitylabs.sdk.api.bft.RootTrustBase;
import org.unicitylabs.sdk.crypto.MintSigningService;
import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.DataHasher;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.crypto.secp256k1.SigningService;
import org.unicitylabs.sdk.predicate.EncodedPredicate;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.builtin.SignaturePredicate;
import org.unicitylabs.sdk.predicate.verification.PredicateVerifierService;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.HexConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * Represents a Mint Transaction.
 *
 * <p>This transaction is responsible for minting new tokens with specific attributes and assigns
 * it to an initial owner.
 */
public class MintTransaction implements Transaction {
  public static final long CBOR_TAG = 39041;
  private static final int VERSION = 1;

  private final MintTransactionState sourceStateHash;
  private final EncodedPredicate lockScript;
  private final NetworkId networkId;
  private final EncodedPredicate recipient;
  private final TokenSalt salt;
  private final TokenType tokenType;
  private final TokenId tokenId;
  private final byte[] justification;
  private final byte[] data;

  private MintTransaction(
          MintTransactionState sourceStateHash,
          EncodedPredicate lockScript,
          NetworkId networkId,
          EncodedPredicate recipient,
          TokenSalt salt,
          TokenType tokenType,
          TokenId tokenId,
          byte[] justification,
          byte[] data
  ) {
    this.sourceStateHash = sourceStateHash;
    this.lockScript = lockScript;
    this.networkId = networkId;
    this.recipient = recipient;
    this.salt = salt;
    this.tokenType = tokenType;
    this.tokenId = tokenId;
    this.justification = justification;
    this.data = data;
  }

  public int getVersion() {
    return MintTransaction.VERSION;
  }


  @Override
  public MintTransactionState getSourceStateHash() {
    return this.sourceStateHash;
  }

  @Override
  public EncodedPredicate getLockScript() {
    return this.lockScript;
  }

  @Override
  public EncodedPredicate getRecipient() {
    return this.recipient;
  }

  /**
   * Retrieves the network identifier.
   *
   * @return the network identifier as a {@code NetworkId}.
   */
  public NetworkId getNetworkId() {
    return this.networkId;
  }

  /**
   * Retrieves the mint-transaction salt.
   *
   * @return the salt as a {@code TokenSalt}.
   */
  public TokenSalt getSalt() {
    return this.salt;
  }

  /**
   * Retrieves the unique token identifier.
   *
   * @return the token identifier as a {@code TokenId}.
   */
  public TokenId getTokenId() {
    return this.tokenId;
  }

  /**
   * Retrieves the type identifier of the token.
   *
   * @return the token type as a {@code TokenType}.
   */
  public TokenType getTokenType() {
    return this.tokenType;
  }

  /**
   * Retrieves the justification for the mint transaction, if any.
   *
   * @return optional justification bytes
   */
  public Optional<byte[]> getJustification() {
    return Optional.ofNullable(this.justification != null ? Arrays.copyOf(this.justification, this.justification.length) : null);
  }

  @Override
  public Optional<byte[]> getData() {
    return Optional.ofNullable(this.data != null ? Arrays.copyOf(this.data, this.data.length) : null);
  }

  @Override
  public StateMask getStateMask() {
    return StateMask.fromBytes(this.tokenId.getBytes());
  }

  /**
   * Create a mint transaction.
   *
   * @param networkId network identifier
   * @param recipient recipient predicate
   * @param data payload bytes, may be null
   * @param tokenType token type identifier
   * @param salt mint-transaction salt
   * @param justification mint justification bytes, may be null
   *
   * @return mint transaction
   */
  public static MintTransaction create(
          NetworkId networkId,
          Predicate recipient,
          byte[] data,
          TokenType tokenType,
          TokenSalt salt,
          byte[] justification
  ) {
    Objects.requireNonNull(networkId, "Network id cannot be null");
    Objects.requireNonNull(recipient, "Recipient cannot be null");
    Objects.requireNonNull(tokenType, "Token type cannot be null");
    Objects.requireNonNull(salt, "Salt cannot be null");

    TokenId tokenId = TokenId.fromSalt(networkId, salt);
    SigningService signingService = MintSigningService.create(tokenId);
    return new MintTransaction(
            MintTransactionState.create(tokenId),
            EncodedPredicate.fromPredicate(SignaturePredicate.fromSigningService(signingService)),
            networkId,
            EncodedPredicate.fromPredicate(recipient),
            salt,
            tokenType,
            tokenId,
            justification != null ? Arrays.copyOf(justification, justification.length) : null,
            data != null ? Arrays.copyOf(data, data.length) : null
    );
  }

  /**
   * Create a mint transaction without a justification.
   *
   * @param networkId network identifier
   * @param recipient recipient predicate
   * @param data payload bytes, may be null
   * @param tokenType token type identifier
   * @param salt mint-transaction salt
   *
   * @return mint transaction
   */
  public static MintTransaction create(
          NetworkId networkId,
          Predicate recipient,
          byte[] data,
          TokenType tokenType,
          TokenSalt salt
  ) {
    return MintTransaction.create(networkId, recipient, data, tokenType, salt, null);
  }

  /**
   * Create a mint transaction with a fresh random salt.
   *
   * @param networkId network identifier
   * @param recipient recipient predicate
   * @param data payload bytes, may be null
   * @param tokenType token type identifier
   *
   * @return mint transaction
   */
  public static MintTransaction create(
          NetworkId networkId,
          Predicate recipient,
          byte[] data,
          TokenType tokenType
  ) {
    return MintTransaction.create(networkId, recipient, data, tokenType, TokenSalt.generate());
  }

  /**
   * Create a mint transaction with a generated token type.
   *
   * @param networkId network identifier
   * @param recipient recipient predicate
   * @param data payload bytes, may be null
   * @param salt mint-transaction salt
   *
   * @return mint transaction
   */
  public static MintTransaction create(
          NetworkId networkId,
          Predicate recipient,
          byte[] data,
          TokenSalt salt
  ) {
    return MintTransaction.create(networkId, recipient, data, TokenType.generate(), salt);
  }

  /**
   * Create a mint transaction with no data.
   *
   * @param networkId network identifier
   * @param recipient recipient predicate
   * @param tokenType token type identifier
   * @param salt mint-transaction salt
   *
   * @return mint transaction
   */
  public static MintTransaction create(
          NetworkId networkId,
          Predicate recipient,
          TokenType tokenType,
          TokenSalt salt
  ) {
    return MintTransaction.create(networkId, recipient, (byte[]) null, tokenType, salt);
  }

  /**
   * Create a mint transaction with a generated token type and salt.
   *
   * @param networkId network identifier
   * @param recipient recipient predicate
   * @param data payload bytes, may be null
   *
   * @return mint transaction
   */
  public static MintTransaction create(NetworkId networkId, Predicate recipient, byte[] data) {
    return MintTransaction.create(networkId, recipient, data, TokenType.generate());
  }

  /**
   * Create a mint transaction with no data and a generated salt.
   *
   * @param networkId network identifier
   * @param recipient recipient predicate
   * @param tokenType token type identifier
   *
   * @return mint transaction
   */
  public static MintTransaction create(
          NetworkId networkId,
          Predicate recipient,
          TokenType tokenType
  ) {
    return MintTransaction.create(networkId, recipient, (byte[]) null, tokenType);
  }

  /**
   * Create a mint transaction with no data and a generated token type.
   *
   * @param networkId network identifier
   * @param recipient recipient predicate
   * @param salt mint-transaction salt
   *
   * @return mint transaction
   */
  public static MintTransaction create(
          NetworkId networkId,
          Predicate recipient,
          TokenSalt salt
  ) {
    return MintTransaction.create(networkId, recipient, TokenType.generate(), salt);
  }

  /**
   * Create a mint transaction with no data, generated token type and salt.
   *
   * @param networkId network identifier
   * @param recipient recipient predicate
   *
   * @return mint transaction
   */
  public static MintTransaction create(NetworkId networkId, Predicate recipient) {
    return MintTransaction.create(networkId, recipient, (byte[]) null);
  }

  /**
   * Deserialize mint transaction from CBOR bytes.
   *
   * @param bytes CBOR bytes
   *
   * @return mint transaction
   */
  public static MintTransaction fromCbor(byte[] bytes) {
    CborDeserializer.CborTag tag = CborDeserializer.decodeTag(bytes);
    if (tag.getTag() != MintTransaction.CBOR_TAG) {
      throw new CborSerializationException(String.format("Invalid CBOR tag: %s", tag.getTag()));
    }
    List<byte[]> data = CborDeserializer.decodeArray(tag.getData(), 7);

    int version = CborDeserializer.decodeUnsignedInteger(data.get(0)).asInt();
    if (version != MintTransaction.VERSION) {
      throw new CborSerializationException(String.format("Unsupported version: %s", version));
    }

    return MintTransaction.create(
            NetworkId.fromId(CborDeserializer.decodeUnsignedInteger(data.get(1)).asShort()),
            EncodedPredicate.fromCbor(data.get(2)),
            CborDeserializer.decodeNullable(data.get(6), CborDeserializer::decodeByteString),
            TokenType.fromCbor(data.get(4)),
            TokenSalt.fromCbor(data.get(3)),
            CborDeserializer.decodeNullable(data.get(5), CborDeserializer::decodeByteString)
    );
  }

  /**
   * Calculate mint transaction state hash.
   *
   * @return state hash
   */
  @Override
  public DataHash calculateStateHash() {
    return new DataHasher(HashAlgorithm.SHA256)
            .update(
                    CborSerializer.encodeArray(
                            CborSerializer.encodeByteString(this.sourceStateHash.getImprint()),
                            this.getStateMask().toCbor()
                    )
            )
            .digest();
  }

  /**
   * Calculate hash of serialized mint transaction.
   *
   * @return transaction hash
   */
  @Override
  public DataHash calculateTransactionHash() {
    return new DataHasher(HashAlgorithm.SHA256).update(this.toCbor()).digest();
  }

  /**
   * Serialize mint transaction to CBOR bytes.
   *
   * @return CBOR bytes
   */
  @Override
  public byte[] toCbor() {
    return CborSerializer.encodeTag(
            MintTransaction.CBOR_TAG,
            CborSerializer.encodeArray(
                    CborSerializer.encodeUnsignedInteger(MintTransaction.VERSION),
                    CborSerializer.encodeUnsignedInteger(this.networkId.getId()),
                    this.recipient.toCbor(),
                    this.salt.toCbor(),
                    this.tokenType.toCbor(),
                    CborSerializer.encodeNullable(this.justification, CborSerializer::encodeByteString),
                    CborSerializer.encodeNullable(this.data, CborSerializer::encodeByteString)
            )
    );
  }

  /**
   * Build certified mint transaction by attaching and verifying inclusion proof.
   *
   * @param trustBase root trust base
   * @param predicateVerifier predicate verifier
   * @param inclusionProof inclusion proof
   *
   * @return certified mint transaction
   */
  public CertifiedMintTransaction toCertifiedTransaction(
          RootTrustBase trustBase,
          PredicateVerifierService predicateVerifier,
          InclusionProof inclusionProof
  ) {
    return CertifiedMintTransaction.fromTransaction(trustBase, predicateVerifier, this,
            inclusionProof);
  }

  @Override
  public String toString() {
    return String.format(
            "MintTransaction{sourceStateHash=%s, lockScript=%s, networkId=%s, recipient=%s, salt=%s, tokenType=%s, tokenId=%s, data=%s}",
            this.sourceStateHash, this.lockScript, this.networkId, this.recipient, this.salt,
            this.tokenType, this.tokenId, HexConverter.encode(this.data));
  }
}
