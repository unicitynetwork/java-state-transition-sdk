package org.unicitylabs.sdk.payment.asset;

import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Asset-id-keyed collection of {@link Asset} values used in a payment. Assets are held in
 * canonical asset-id order — the order the split protocol requires — so {@link #toList()} and
 * {@link #toCbor()} always produce that order.
 */
public final class PaymentAssetCollection {

  public static final int MIN_SIZE = 1;
  public static final int MAX_SIZE = 256;

  private final Map<AssetId, Asset> assets;

  private PaymentAssetCollection(Map<AssetId, Asset> assets) {
    this.assets = assets;
  }

  /**
   * Create a PaymentAssetCollection. Assets may be supplied in any order; they are stored
   * canonically.
   *
   * @param assets assets to include (1..256, distinct ids)
   * @return new collection
   * @throws IllegalArgumentException if the count is out of range, or an asset id repeats
   */
  public static PaymentAssetCollection create(Asset... assets) {
    List<Asset> sorted = new ArrayList<>(Arrays.asList(assets));
    sorted.sort(PaymentAssetCollection::compareAssets);
    return PaymentAssetCollection.fromList(sorted);
  }

  /**
   * Create PaymentAssetCollection from CBOR bytes. The encoded assets MUST be in strict canonical
   * asset-id order with no duplicates.
   *
   * @param bytes CBOR bytes
   * @return decoded collection
   * @throws CborSerializationException if the assets are not in strict canonical asset-id order
   */
  public static PaymentAssetCollection fromCbor(byte[] bytes) {
    List<Asset> assets = CborDeserializer.decodeArray(bytes).stream()
            .map(Asset::fromCbor)
            .collect(Collectors.toList());
    for (int i = 1; i < assets.size(); i++) {
      if (PaymentAssetCollection.compareAssets(assets.get(i - 1), assets.get(i)) >= 0) {
        throw new CborSerializationException(
                "Payment assets must be in strict canonical asset-id order.");
      }
    }

    return PaymentAssetCollection.fromList(assets);
  }

  /**
   * Compare two assets by their asset id in canonical order: ascending unsigned lexicographic
   * order of the raw id bytes, a shorter id ordered before a longer one that it is a prefix of.
   */
  private static int compareAssets(Asset a, Asset b) {
    byte[] x = a.getId().getBytes();
    byte[] y = b.getId().getBytes();
    int length = Math.min(x.length, y.length);
    for (int i = 0; i < length; i++) {
      int diff = (x[i] & 0xFF) - (y[i] & 0xFF);
      if (diff != 0) {
        return diff;
      }
    }

    return x.length - y.length;
  }

  private static PaymentAssetCollection fromList(List<Asset> assets) {
    if (assets.size() < PaymentAssetCollection.MIN_SIZE
            || assets.size() > PaymentAssetCollection.MAX_SIZE) {
      throw new IllegalArgumentException(
              String.format("Payment asset collection must hold between %d and %d assets, got %d.",
                      PaymentAssetCollection.MIN_SIZE, PaymentAssetCollection.MAX_SIZE,
                      assets.size()));
    }

    Map<AssetId, Asset> map = new LinkedHashMap<>();
    for (Asset asset : assets) {
      if (map.putIfAbsent(asset.getId(), asset) != null) {
        throw new IllegalArgumentException(
                "Invalid payment asset collection. Duplicate assets found.");
      }
    }

    return new PaymentAssetCollection(map);
  }

  /**
   * Look up the asset with the given id.
   *
   * @param id asset id
   * @return matching asset, or {@code null}
   */
  public Asset get(AssetId id) {
    return this.assets.get(id);
  }

  /**
   * Get the number of assets in this collection.
   *
   * @return asset count
   */
  public int size() {
    return this.assets.size();
  }

  /**
   * Get the assets in canonical asset-id order.
   *
   * @return assets
   */
  public List<Asset> toList() {
    return List.copyOf(this.assets.values());
  }

  /**
   * Serialize this collection to CBOR bytes (assets in canonical order).
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
            this.assets.values().stream().map(Asset::toCbor).toArray(byte[][]::new));
  }
}
