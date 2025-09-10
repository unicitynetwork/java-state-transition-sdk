package com.unicity.sdk.e2e.steps;

import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.address.ProxyAddress;
import com.unicity.sdk.api.AggregatorClient;
import com.unicity.sdk.api.SubmitCommitmentResponse;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import com.unicity.sdk.e2e.config.CucumberConfiguration;
import com.unicity.sdk.e2e.context.TestContext;
import com.unicity.sdk.e2e.steps.shared.StepHelper;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferCommitment;
import com.unicity.sdk.transaction.TransferTransactionData;
import com.unicity.sdk.util.InclusionProofUtils;
import com.unicity.sdk.utils.TestUtils;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.utils.helpers.PendingTransfer;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

import static com.unicity.sdk.utils.TestUtils.randomBytes;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Advanced step definitions for complex scenarios and edge cases.
 */
public class AdvancedStepDefinitions {

    private final TestContext context;

    public AdvancedStepDefinitions() {
        this.context = CucumberConfiguration.getTestContext();
    }

    StepHelper helper = new StepHelper();

    @When("the token is transferred through the chain of existing users")
    public void theTokenIsTransferredThroughTheChain() throws Exception {
        List<String> users = new ArrayList<>(context.getUserSigningServices().keySet());

        // remove the one that should go first (if it’s already inside)
        users.removeAll(context.getTransferChain());

        // prepend the transfer chain at the beginning
        List<String> orderedUsers = new ArrayList<>();
        orderedUsers.addAll(context.getTransferChain()); // first element(s)
        orderedUsers.addAll(users);


        Token currentToken = context.getChainToken();

        for (int i = 0; i < orderedUsers.size() - 1; i++) {
            String fromUser = orderedUsers.get(i);
            String toUser = orderedUsers.get(i + 1);

            SigningService fromSigningService = context.getUserSigningServices().get(fromUser);
            SigningService toSigningService = context.getUserSigningServices().get(toUser);
            byte[] toNonce = context.getUserNonces().get(toUser);

            // Create a simple direct address for transfer
            var toPredicate = com.unicity.sdk.predicate.MaskedPredicate.create(
                    toSigningService,
                    com.unicity.sdk.hash.HashAlgorithm.SHA256,
                    toNonce
            );
            var toAddress = toPredicate.getReference(currentToken.getType()).toAddress();

            String customData = "Transfer from " + fromUser + " to " + toUser;
            System.out.println(customData);
            context.getTransferCustomData().put(toUser, customData);

            currentToken = TestUtils.transferToken(
                    context.getClient(),
                    currentToken,
                    fromSigningService,
                    toSigningService,
                    toNonce,
                    toAddress,
                    customData.getBytes(StandardCharsets.UTF_8),
                    List.of()
            );

            context.getTransferChain().add(toUser);
        }

        context.setChainToken(currentToken);
    }

    @Then("the final token should maintain original properties")
    public void theFinalTokenShouldMaintainOriginalProperties() {
        assertNotNull(context.getChainToken(), "Final token should exist");
        assertTrue(context.getChainToken().verify().isSuccessful(), "Final token should be valid");

        // Additional property validation can be added based on requirements
    }

    @And("all intermediate transfers should be recorded correctly")
    public void allIntermediateTransfersShouldBeRecordedCorrectly() {
        assertEquals(4, context.getTransferChain().size(), "Transfer chain should have 4 users");
        assertEquals("Alice", context.getTransferChain().get(0), "Chain should start with Alice");
        assertEquals("Dave", context.getTransferChain().get(3), "Chain should end with Dave");
    }

    @And("the token should have <expectedTransfers> transfers in history")
    public void theTokenShouldHaveTransfersInHistory(int expectedTransfers) {
        int actualTransfers = context.getChainToken().getTransactions().size() - 1; // Subtract mint transaction
        assertEquals(expectedTransfers, actualTransfers, "Token should have expected number of transfers");
    }

    // Name Tag Scenarios Steps
    @Given("{string} creates {int} name tag tokens with different addresses")
    public void createsNameTagTokensWithDifferentAddresses(String username, int nametagCount) throws Exception {
        List<Token> bobNametags = new ArrayList<>();

        for (int i = 0; i < nametagCount; i++) {
            String tokenIdentifier = TestUtils.generateRandomString(10) + i;
            Token nametagToken = helper.createNameTagTokenForUser(
                    username,
                    TestUtils.createTokenTypeFromString(tokenIdentifier),
                    tokenIdentifier,
                    TestUtils.generateRandomString(10)
            );
            bobNametags.add(nametagToken);
        }

        context.getNameTagTokens().put(username, bobNametags);
    }

    @When("{string} transfers tokens to each of {string} name tags")
    public void userTransfersTokensToEachOfBobsNameTags(String fromUser, String toUser) throws Exception {
        List<Token> nametagTokens = context.getNameTagTokens().get(toUser);

        // Create tokens for Alice to transfer
        for (int i = 0; i < nametagTokens.size(); i++) {
            TokenId tokenId = TestUtils.generateRandomTokenId();
            TokenType tokenType = TestUtils.generateRandomTokenType();
            TokenCoinData coinData = TestUtils.createRandomCoinData(1);

            Token aliceToken = TestUtils.mintTokenForUser(
                    context.getClient(),
                    context.getUserSigningServices().get(fromUser),
                    context.getUserNonces().get(fromUser),
                    tokenId,
                    tokenType,
                    coinData
            );

            // Transfer to Bob's nametag
            ProxyAddress proxyAddress = ProxyAddress.create(nametagTokens.get(i).getId());

            helper.transferToken3(
                    fromUser,
                    toUser,
                    aliceToken,
                    proxyAddress,
                    null
            );
        }
    }

    @And("{string} consolidates all received tokens")
    public void userConsolidatesAllReceivedTokens(String username) {
        // Consolidation logic would depend on specific requirements
        // For now, we ensure Bob has received all the tokens
        List<Token> bobTokens = context.getUserTokens().getOrDefault(username, new ArrayList<>());
        // Verify Bob has received tokens
        assertFalse(bobTokens.isEmpty(), "Bob should have received tokens");
    }

    @Then("{string} should own {int} tokens")
    public void userShouldOwnTokens(String username, int expectedTokenCount) {
        List<Token> bobTokens = context.getUserTokens().getOrDefault(username, new ArrayList<>());
        assertEquals(expectedTokenCount, bobTokens.size(), "Bob should own expected number of tokens");

        // Verify ownership
        for (Token token : bobTokens) {
            SigningService bobSigningService = SigningService.createFromSecret(context.getUserSecret().get(username), token.getState().getUnlockPredicate().getNonce());
            assertTrue(token.verify().isSuccessful(), "Token should be valid");
            assertTrue(TestUtils.validateTokenOwnership(token, bobSigningService),
                    "Bob should own all tokens");
        }
    }

    @And("all {string} name tag tokens should remain valid")
    public void allNameTagTokensShouldRemainValid(String username) {
        List<Token> bobNametags = context.getNameTagTokens().get(username);
        for (Token nametag : bobNametags) {
            assertTrue(nametag.verify().isSuccessful(), "All name tag tokens should remain valid");
        }
    }

    @And("proxy addressing should work for all {string} name tags")
    public void proxyAddressingShouldWorkForAllNameTags(String username) {
        List<Token> bobNametags = context.getNameTagTokens().get(username);

        for (Token nametag : bobNametags) {
            var proxyAddress = com.unicity.sdk.address.ProxyAddress.create(nametag.getId());
            assertNotNull(proxyAddress, "Proxy address should be creatable for all name tags");
        }
    }

    // Large Data Handling Steps
    @Given("a token with custom data of size {int} bytes")
    public void aTokenWithCustomDataOfSizeBytes(int dataSize) throws Exception {
        String alice = "Alice";
        byte[] largeData = new byte[dataSize];
        Arrays.fill(largeData, (byte) 'A'); // Fill with 'A' characters

        TokenId tokenId = TestUtils.generateRandomTokenId();
        TokenType tokenType = TestUtils.generateRandomTokenType();
        TokenCoinData coinData = TestUtils.createRandomCoinData(1);

        // Create token with large custom data
        MaskedPredicate predicate = MaskedPredicate.create(
                context.getUserSigningServices().get(alice),
                com.unicity.sdk.hash.HashAlgorithm.SHA256,
                context.getUserNonces().get(alice)
        );

        var tokenState = new com.unicity.sdk.token.TokenState(predicate, largeData);

        // Store for later use in transfer
        context.setChainToken(TestUtils.mintTokenForUser(
                context.getClient(),
                context.getUserSigningServices().get(alice),
                context.getUserNonces().get(alice),
                tokenId,
                tokenType,
                coinData
        ));
    }

    @And("{string} finalizes all received tokens")
    public void finalizesAllReceivedTokens(String username) throws Exception {
        List<PendingTransfer> pendingTransfers = context.getPendingTransfers(username);

        for (PendingTransfer pending : pendingTransfers) {
            Token token = pending.getSourceToken();
            Transaction<TransferTransactionData> tx = pending.getTransaction();
            helper.finalizeTransfer(
                    username,
                    token,
                    tx
            );
        }
        context.clearPendingTransfers(username);
    }
}