package org.unicitylabs.sdk.payment;

import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.DataHasher;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.payment.asset.Asset;
import org.unicitylabs.sdk.payment.asset.AssetId;
import org.unicitylabs.sdk.predicate.EncodedPredicate;
import org.unicitylabs.sdk.predicate.builtin.BurnPredicate;
import org.unicitylabs.sdk.transaction.CertifiedMintTransaction;
import org.unicitylabs.sdk.transaction.CertifiedTransferTransaction;
import org.unicitylabs.sdk.transaction.Token;
import org.unicitylabs.sdk.transaction.verification.MintJustificationVerifier;
import org.unicitylabs.sdk.util.verification.VerificationResult;
import org.unicitylabs.sdk.util.verification.VerificationStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Verifier for {@link SplitMintJustification} mint justifications. It reports the burned source
 * token for caller verification, binds the burn to the split manifest, recomputes the output's
 * leaf data, and verifies every output asset against its allocation proof — requiring each
 * asset's reconstructed total to equal the source amount (value conservation).
 */
public class SplitMintJustificationVerifier implements MintJustificationVerifier {

  private static final String RULE = "SplitMintJustificationVerifier";

  private final PaymentDataDeserializer decodePaymentData;

  public SplitMintJustificationVerifier(PaymentDataDeserializer decodePaymentData) {
    this.decodePaymentData = Objects.requireNonNull(decodePaymentData,
            "decodePaymentData cannot be null");
  }

  @Override
  public long getTag() {
    return SplitMintJustification.CBOR_TAG;
  }

  private static VerificationResult<VerificationStatus> fail(String message) {
    return new VerificationResult<>(SplitMintJustificationVerifier.RULE, VerificationStatus.FAIL,
            message);
  }

  @Override
  public VerificationResult<VerificationStatus> verify(CertifiedMintTransaction transaction,
                                                       Consumer<Token> nestedTokenCollector) {
    Objects.requireNonNull(transaction, "transaction cannot be null");
    Objects.requireNonNull(nestedTokenCollector, "nestedTokenCollector cannot be null");

    byte[] justificationBytes = transaction.getJustification().orElse(null);
    if (justificationBytes == null) {
      return SplitMintJustificationVerifier.fail("Transaction has no justification.");
    }

    SplitMintJustification justification = SplitMintJustification.fromCbor(justificationBytes);

    if (!transaction.getNetworkId().equals(justification.getToken().getGenesis().getNetworkId())) {
      return SplitMintJustificationVerifier.fail(
              String.format(
                      "Network identifier mismatch: mint is on %s, source token is on %s.",
                      transaction.getNetworkId(),
                      justification.getToken().getGenesis().getNetworkId()
              )
      );
    }

    nestedTokenCollector.accept(justification.getToken());

    List<CertifiedTransferTransaction> transfers = justification.getToken().getTransactions();
    if (transfers.isEmpty()) {
      return SplitMintJustificationVerifier.fail(
              "Burned source token does not end in a certified transfer.");
    }
    CertifiedTransferTransaction burnTransaction = transfers.get(transfers.size() - 1);

    byte[] manifestBytes = burnTransaction.getData().orElse(null);
    if (manifestBytes == null) {
      return SplitMintJustificationVerifier.fail("Burn transfer has no manifest.");
    }
    List<DataHash> roots = SplitManifest.fromCbor(manifestBytes).getRoots();

    DataHash burnReason = new DataHasher(HashAlgorithm.SHA256).update(manifestBytes).digest();
    EncodedPredicate expectedRecipient = EncodedPredicate.fromPredicate(
            BurnPredicate.create(burnReason.getData()));
    if (!expectedRecipient.equals(burnTransaction.getRecipient())) {
      return SplitMintJustificationVerifier.fail(
              "Burn transfer recipient does not match the manifest hash.");
    }

    if (!transaction.getTokenType().equals(justification.getToken().getType())) {
      return SplitMintJustificationVerifier.fail(
              "Output token type does not match the source token type.");
    }

    byte[] sourcePaymentBytes = justification.getToken().getGenesis().getData().orElse(null);
    PaymentData sourceTokenPaymentData = sourcePaymentBytes != null
            ? this.decodePaymentData.decode(sourcePaymentBytes)
            : null;
    if (sourceTokenPaymentData == null
            || sourceTokenPaymentData.getAssets().size() != roots.size()) {
      return SplitMintJustificationVerifier.fail(
              "Manifest root count does not match the source asset count.");
    }

    byte[] paymentDataBytes = transaction.getData().orElse(null);
    PaymentData paymentData = paymentDataBytes != null
            ? this.decodePaymentData.decode(paymentDataBytes)
            : null;
    if (paymentData == null
            || justification.getProofs().size() != paymentData.getAssets().size()) {
      return SplitMintJustificationVerifier.fail(
              "Allocation proof count does not match the output asset count.");
    }

    byte[] leafData = SplitMintJustification.calculateLeafData(
            justification.getToken(),
            transaction.getRecipient(),
            transaction.getSalt(),
            transaction.getTokenId(),
            paymentDataBytes
    );

    List<Asset> assets = paymentData.getAssets().toList();

    Map<AssetId, DataHash> rootByAsset = new LinkedHashMap<>();
    List<Asset> sourceAssets = sourceTokenPaymentData.getAssets().toList();
    for (int i = 0; i < sourceAssets.size(); i++) {
      rootByAsset.put(sourceAssets.get(i).getId(), roots.get(i));
    }

    for (int i = 0; i < assets.size(); i++) {
      Asset asset = assets.get(i);
      Asset sourceAsset = sourceTokenPaymentData.getAssets().get(asset.getId());
      DataHash root = rootByAsset.get(asset.getId());
      if (sourceAsset == null || root == null) {
        return SplitMintJustificationVerifier.fail(
                String.format("Asset %s is absent from the source token.", asset.getId()));
      }

      boolean isProofValid = justification.getProofs().get(i).verify(
              transaction.getTokenId().getBytes(),
              leafData,
              asset.getValue(),
              root,
              sourceAsset.getValue()
      );
      if (!isProofValid) {
        return SplitMintJustificationVerifier.fail(
                String.format("Allocation proof failed for asset %s.", asset.getId()));
      }
    }

    return new VerificationResult<>(SplitMintJustificationVerifier.RULE, VerificationStatus.OK);
  }
}
