
package com.unicity.sdk;

import com.unicity.sdk.address.Address;
import com.unicity.sdk.address.ProxyAddress;
import com.unicity.sdk.api.AggregatorClient;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.api.SubmitCommitmentResponse;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.InclusionProofVerificationStatus;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransactionData;
import com.unicity.sdk.transaction.TransferTransactionData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class StateTransitionClient {

  protected final AggregatorClient client;

  public StateTransitionClient(AggregatorClient client) {
    this.client = client;
  }

  public <T extends MintTransactionData<?>> CompletableFuture<SubmitCommitmentResponse> submitCommitment(
      Commitment<T> commitment) {
    return this.client.submitCommitment(
        commitment.getRequestId(),
        commitment.getTransactionData().calculateHash(),
        commitment.getAuthenticator()
    );
  }

  public CompletableFuture<SubmitCommitmentResponse> submitCommitment(
      Token<?> token,
      Commitment<TransferTransactionData> commitment) {
    if (!commitment.getTransactionData().getSourceState().getUnlockPredicate()
        .isOwner(commitment.getAuthenticator().getPublicKey())) {
      throw new IllegalArgumentException(
          "Ownership verification failed: Authenticator does not match source state predicate.");
    }

    return this.client.submitCommitment(commitment.getRequestId(), commitment.getTransactionData()
        .calculateHash(token.getId(), token.getType()), commitment.getAuthenticator());
  }

  public <T extends MintTransactionData<?>> Transaction<T> createTransaction(
      Commitment<T> commitment,
      InclusionProof inclusionProof) {
    return this.createTransaction(
        commitment.getRequestId(),
        commitment.getTransactionData(),
        commitment.getTransactionData()
            .calculateHash(),
        inclusionProof
    );
  }

  public <T extends Transaction<MintTransactionData<?>>> Transaction<TransferTransactionData> createTransaction(
      Token<T> token,
      Commitment<TransferTransactionData> commitment,
      InclusionProof inclusionProof) {
    return this.createTransaction(
        commitment.getRequestId(),
        commitment.getTransactionData(),
        commitment.getTransactionData()
            .calculateHash(token.getId(), token.getType()),
        inclusionProof
    );
  }

  private <T extends TransactionData<?>> Transaction<T> createTransaction(
      RequestId requestId,
      T transactionData,
      DataHash transactionHash,
      InclusionProof inclusionProof) {
    if (inclusionProof.verify(requestId) != InclusionProofVerificationStatus.OK) {
      throw new RuntimeException("Inclusion proof verification failed.");
    }

    if (inclusionProof.getAuthenticator() == null) {
      throw new RuntimeException("Authenticator is missing from inclusion proof.");
    }

    if (!inclusionProof.getTransactionHash().equals(transactionHash)) {
      throw new RuntimeException("Payload hash mismatch.");
    }

    return new Transaction<>(transactionData, inclusionProof);
  }

  public <T extends Transaction<MintTransactionData<?>>> Token<T> finishTransaction(
      Token<T> token,
      TokenState state,
      Transaction<TransferTransactionData> transaction
  ) {
    return this.finishTransaction(token, state, transaction, List.of());
  }

  public <T extends Transaction<MintTransactionData<?>>> Token<T> finishTransaction(
      Token<T> token,
      TokenState state,
      Transaction<TransferTransactionData> transaction,
      List<Token<?>> nametagTokens
  ) {
    Objects.requireNonNull(token, "Token is null");
    Objects.requireNonNull(state, "State is null");
    Objects.requireNonNull(transaction, "Transaction is null");
    Objects.requireNonNull(nametagTokens, "Nametag tokens are null");

    if (!transaction.getData().getSourceState().getUnlockPredicate()
        .verify(transaction, token.getId(), token.getType())) {
      throw new RuntimeException("Predicate verification failed");
    }

    Map<Address, Token<?>> nametags = new HashMap<>();
    for (Token<?> nametagToken : nametagTokens) {
      ProxyAddress address = ProxyAddress.create(nametagToken.getId());
      if (nametags.containsKey(address)) {
        throw new RuntimeException("Duplicate nametag in list");
      }

      nametags.put(address, nametagToken);
    }

    Address recipient = ProxyAddress.resolve(transaction.getData().getRecipient(), nametags);
    if (!state.getUnlockPredicate().getReference(token.getType()).toAddress().equals(recipient)) {
      throw new RuntimeException("Recipient address mismatch");
    }

    if (!transaction.containsData(state.getData())) {
      throw new RuntimeException("State data is not part of transaction.");
    }

    List<Transaction<TransferTransactionData>> transactions = new ArrayList<>(
        token.getTransactions());
    transactions.add(transaction);

    return new Token<>(state, token.getGenesis(), transactions, nametagTokens);
  }

  public CompletableFuture<InclusionProofVerificationStatus> getTokenStatus(
      Token<? extends Transaction<MintTransactionData<?>>> token,
      byte[] publicKey) {
    RequestId requestId = RequestId.create(publicKey,
        token.getState().calculateHash(token.getId(), token.getType()));
    return this.client.getInclusionProof(requestId)
        .thenApply(inclusionProof -> inclusionProof.verify(requestId));
  }

  public CompletableFuture<InclusionProof> getInclusionProof(Commitment<?> commitment) {
    return this.client.getInclusionProof(commitment.getRequestId());
  }
}
