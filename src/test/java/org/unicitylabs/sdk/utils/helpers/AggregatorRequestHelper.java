package org.unicitylabs.sdk.utils.helpers;

import org.unicitylabs.sdk.api.*;
import org.unicitylabs.sdk.hash.DataHash;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AggregatorRequestHelper {
    private final int shardIdLength;
    private final Map<Integer, AggregatorClient> shardMap = new HashMap<>();
    private final Map<Integer, String> shardUrls = new HashMap<>();
    private final Map<Integer, ShardStats> shardStats = new HashMap<>();

    public AggregatorRequestHelper(int shardIdLength, List<AggregatorClient> clients, List<String> urls) {
        this.shardIdLength = shardIdLength;

        int baseId = 1 << shardIdLength; // e.g. 4 for shardIdLength=2
        for (int i = 0; i < clients.size(); i++) {
            int shardId = baseId + i; // 4,5,6,7 for length=2
            shardMap.put(shardId, clients.get(i));
            shardUrls.put(shardId, urls.get(i));
            shardStats.put(shardId, new ShardStats());
        }
    }

    public String getShardUrl(int shardId) {
        return shardUrls.getOrDefault(shardId, "unknown");
    }

    public ShardStats getStats(int shardId) {
        return shardStats.get(shardId);
    }

    private int getShardIdFromClient(AggregatorClient client) {
        // Optional: parse from client URL like ...:300X
        return shardMap.entrySet().stream()
                .filter(e -> e.getValue().equals(client))
                .map(Map.Entry::getKey)
                .findFirst().orElse(-1);
    }

    public int getShardForRequest(RequestId requestId) {
        // Use raw binary data (actual SHA256 bytes)
        byte[] imprint = requestId.getImprint();

        // Convert to BigInteger (unsigned)
        BigInteger idNum = new BigInteger(1, imprint);

        // Extract least significant bits to determine shard
        int shardBits = idNum.intValue() & ((1 << shardIdLength) - 1);

        // Add leading 1-bit prefix (as per spec)
        return (1 << shardIdLength) | shardBits;
    }

    public SubmitCommitmentResponse sendCommitment(
            RequestId requestId, DataHash txDataHash, Authenticator auth) throws Exception {

        int shardId = ShardRoutingUtils.getShardForRequest(requestId, shardIdLength);
        AggregatorClient client = shardMap.get(shardId);
        shardStats.get(shardId).incrementCommitments();

        System.out.printf("→ Sending commitment to shard %d (%s)%n", shardId, getShardUrl(shardId));

        SubmitCommitmentResponse response = client.submitCommitment(requestId, txDataHash, auth).get();
        if (response.getStatus() == SubmitCommitmentStatus.SUCCESS)
            shardStats.get(shardId).incrementSuccess();
        else
            shardStats.get(shardId).incrementFailures();

        return response;
    }

    public void printShardStats() {
        System.out.println("\n=== Shard statistics ===");
        shardStats.forEach((id, s) -> System.out.printf(
                "Shard %d → total: %d, success: %d, failed: %d%n",
                id, s.getTotal(), s.getSuccess(), s.getFailures()));
    }

    public static class ShardStats {
        private final AtomicInteger total = new AtomicInteger();
        private final AtomicInteger success = new AtomicInteger();
        private final AtomicInteger failures = new AtomicInteger();

        public void incrementCommitments() { total.incrementAndGet(); }
        public void incrementSuccess() { success.incrementAndGet(); }
        public void incrementFailures() { failures.incrementAndGet(); }

        public void incrementSuccessBy(int n) { success.addAndGet(n); }
        public void incrementFailuresBy(int n) { failures.addAndGet(n); }


        public int getTotal() { return total.get(); }
        public int getSuccess() { return success.get(); }
        public int getFailures() { return failures.get(); }
    }

    public int getShardIdLength() {
        return shardIdLength;
    }

    public AggregatorClient getClientForShard(int shardId) {
        return shardMap.get(shardId);
    }
}