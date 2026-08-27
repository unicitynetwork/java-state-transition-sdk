package org.unicitylabs.sdk.interop;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.TestAggregatorClient;
import org.unicitylabs.sdk.api.CertificationData;
import org.unicitylabs.sdk.api.CertificationStatus;
import org.unicitylabs.sdk.api.NetworkId;
import org.unicitylabs.sdk.crypto.secp256k1.SigningService;
import org.unicitylabs.sdk.predicate.UnlockScript;
import org.unicitylabs.sdk.predicate.builtin.SignaturePredicate;
import org.unicitylabs.sdk.predicate.builtin.SignaturePredicateUnlockScript;
import org.unicitylabs.sdk.predicate.verification.PredicateVerifierService;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.transaction.StateMask;
import org.unicitylabs.sdk.transaction.Token;
import org.unicitylabs.sdk.transaction.TokenSalt;
import org.unicitylabs.sdk.transaction.TokenType;
import org.unicitylabs.sdk.transaction.TransferTransaction;
import org.unicitylabs.sdk.transaction.verification.MintJustificationVerifierService;
import org.unicitylabs.sdk.transaction.verification.TokenIssuanceVerifierService;
import org.unicitylabs.sdk.transaction.verification.VerificationContext;
import org.unicitylabs.sdk.util.HexConverter;
import org.unicitylabs.sdk.util.InclusionProofUtils;

/**
 * Shared constants and helpers for the cross-SDK interop vectors.
 *
 * <p>Every input here is fixed. A vector is only useful if regenerating it reproduces the same
 * bytes, so nothing may read a clock or a random source: keys, salt, token type, state mask, the
 * request deadline and the aggregator's round clock are all constants, and both SDKs sign with
 * RFC 6979 deterministic ECDSA.
 */
public final class InteropFixture {

  /** Round clock the fake aggregator certifies these vectors under. */
  public static final long REFERENCE_TIME = 1755000000L;
  /** Deadline carried by every request in the vectors. An hour after the round clock. */
  public static final long EXPIRES_AT = REFERENCE_TIME + 3600L;

  public static final byte[] AGGREGATOR_KEY = key(0x01);
  public static final byte[] ALICE_KEY = key(0x02);
  public static final byte[] BOB_KEY = key(0x03);

  /** Directory the vectors live in, as test resources. */
  public static final Path VECTORS = Paths.get("src", "test", "resources", "interop");

  private InteropFixture() {
  }

  private static byte[] key(int last) {
    byte[] bytes = new byte[32];
    bytes[31] = (byte) last;
    return bytes;
  }

  /** Fixed 32-byte value, so salts and token types are reproducible. */
  public static byte[] filled(int value) {
    byte[] bytes = new byte[32];
    java.util.Arrays.fill(bytes, (byte) value);
    return bytes;
  }

  /**
   * Mint a token and transfer it once, entirely from fixed inputs.
   *
   * @return a token with one genesis and one transfer
   * @throws Exception if certification or verification fails
   */
  public static Token buildToken() throws Exception {
    TestAggregatorClient aggregator = TestAggregatorClient.create(AGGREGATOR_KEY);
    aggregator.setReferenceTime(REFERENCE_TIME);
    StateTransitionClient client = new StateTransitionClient(aggregator);
    VerificationContext context = new VerificationContext(
            aggregator.getTrustBase(),
            PredicateVerifierService.create(),
            new MintJustificationVerifierService(),
            new TokenIssuanceVerifierService(false));

    SigningService alice = new SigningService(ALICE_KEY);
    SigningService bob = new SigningService(BOB_KEY);

    MintTransaction mint = MintTransaction.builder(
                    NetworkId.LOCAL, SignaturePredicate.fromSigningService(alice))
            .tokenType(new TokenType(filled(0x11)))
            .salt(TokenSalt.fromBytes(filled(0x22)))
            .expiresAt(EXPIRES_AT)
            .build();

    CertificationData mintData = CertificationData.fromMintTransaction(mint);
    if (client.submitCertificationRequest(mintData).get().getStatus()
            != CertificationStatus.SUCCESS) {
      throw new IllegalStateException("mint was not certified");
    }

    Token token = Token.mint(
            mint.toCertifiedTransaction(
                    context.getTrustBase(),
                    context.getPredicateVerifier(),
                    InclusionProofUtils.waitInclusionProof(
                            client, context.getTrustBase(), context.getPredicateVerifier(),
                            mint).get()),
            context);

    TransferTransaction transfer = TransferTransaction.create(
            token,
            SignaturePredicate.fromSigningService(bob),
            StateMask.fromBytes(filled(0x33)),
            null,
            EXPIRES_AT);
    UnlockScript unlockScript = SignaturePredicateUnlockScript.create(transfer, alice);

    if (client.submitCertificationRequest(
            CertificationData.fromTransaction(transfer, unlockScript)).get().getStatus()
            != CertificationStatus.SUCCESS) {
      throw new IllegalStateException("transfer was not certified");
    }

    return token.transfer(
            transfer.toCertifiedTransaction(
                    context.getTrustBase(),
                    context.getPredicateVerifier(),
                    InclusionProofUtils.waitInclusionProof(
                            client, context.getTrustBase(), context.getPredicateVerifier(),
                            transfer).get()),
            context);
  }

  /**
   * The fake aggregator's trust base, as JSON, for the consuming side to load.
   *
   * @return trust base JSON
   */
  public static String trustBaseJson() {
    return TestAggregatorClient.create(AGGREGATOR_KEY).getTrustBase().toJson();
  }

  /**
   * Read a vector from the resources directory.
   *
   * @param name file name
   * @return file contents
   * @throws IOException if the file cannot be read
   */
  public static byte[] read(String name) throws IOException {
    return Files.readAllBytes(VECTORS.resolve(name));
  }

  /**
   * Read a vector as UTF-8 text.
   *
   * @param name file name
   * @return file contents
   * @throws IOException if the file cannot be read
   */
  public static String readText(String name) throws IOException {
    return new String(read(name), StandardCharsets.UTF_8).trim();
  }

  /**
   * Write a vector, creating the directory if needed.
   *
   * @param name file name
   * @param content file contents
   * @throws IOException if the file cannot be written
   */
  public static void write(String name, byte[] content) throws IOException {
    Files.createDirectories(VECTORS);
    Files.write(VECTORS.resolve(name), content);
  }

  /**
   * Hex-encode for a readable assertion failure.
   *
   * @param bytes bytes to encode
   * @return hex string
   */
  public static String hex(byte[] bytes) {
    return HexConverter.encode(bytes);
  }
}
