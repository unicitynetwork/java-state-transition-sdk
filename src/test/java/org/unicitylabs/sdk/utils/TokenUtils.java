package org.unicitylabs.sdk.utils;

import org.junit.jupiter.api.Assertions;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.api.CertificationData;
import org.unicitylabs.sdk.api.CertificationResponse;
import org.unicitylabs.sdk.api.CertificationStatus;
import org.unicitylabs.sdk.api.NetworkId;
import org.unicitylabs.sdk.crypto.secp256k1.SigningService;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.UnlockScript;
import org.unicitylabs.sdk.predicate.builtin.SignaturePredicateUnlockScript;
import org.unicitylabs.sdk.transaction.*;
import org.unicitylabs.sdk.transaction.verification.VerificationContext;
import org.unicitylabs.sdk.util.InclusionProofUtils;
import org.unicitylabs.sdk.util.verification.VerificationStatus;


/**
 * Test helpers for minting and transferring certified tokens.
 */
public class TokenUtils {

  public static Token mintToken(
          StateTransitionClient client,
          VerificationContext context,
          Predicate recipient
  ) throws Exception {
    return TokenUtils.mintToken(client, context, recipient, (byte[]) null);
  }

  public static Token mintToken(
          StateTransitionClient client,
          VerificationContext context,
          Predicate recipient,
          byte[] data
  ) throws Exception {
    return TokenUtils.mintToken(client, context, recipient, data, NetworkId.LOCAL);
  }

  public static Token mintToken(
          StateTransitionClient client,
          VerificationContext context,
          Predicate recipient,
          byte[] data,
          NetworkId networkId
  ) throws Exception {
    return TokenUtils.mintToken(client, context, recipient, data, networkId, TokenType.generate());
  }

  public static Token mintToken(
          StateTransitionClient client,
          VerificationContext context,
          Predicate recipient,
          byte[] data,
          NetworkId networkId,
          TokenType tokenType
  ) throws Exception {
    return TokenUtils.mintToken(client, context, recipient, data, networkId, tokenType,
            TokenSalt.generate());
  }

  public static Token mintToken(
          StateTransitionClient client,
          VerificationContext context,
          Predicate recipient,
          byte[] data,
          NetworkId networkId,
          TokenType tokenType,
          TokenSalt salt
  ) throws Exception {
    return TokenUtils.mintToken(client, context, recipient, data, networkId, tokenType, salt, null);
  }

  public static Token mintToken(
          StateTransitionClient client,
          VerificationContext context,
          Predicate recipient,
          byte[] data,
          NetworkId networkId,
          TokenType tokenType,
          TokenSalt salt,
          byte[] justification
  ) throws Exception {
    MintTransaction transaction = MintTransaction.builder(networkId, recipient)
            .tokenType(tokenType)
            .salt(salt)
            .data(data)
            .justification(justification)
            .build();

    CertificationData certificationData = CertificationData.fromMintTransaction(transaction);

    CertificationResponse response = client.submitCertificationRequest(certificationData).get();
    if (response.getStatus() != CertificationStatus.SUCCESS) {
      throw new RuntimeException(
              String.format("Certification Request failed with status '%s'", response.getStatus()));
    }

    return Token.mint(
            transaction.toCertifiedTransaction(
                    context.getTrustBase(),
                    context.getPredicateVerifier(),
                    InclusionProofUtils.waitInclusionProof(
                            client, context.getTrustBase(), context.getPredicateVerifier(),
                            transaction).get()
            ),
            context
    );
  }


  /**
   * Deserialize token, build transfer transaction and submit certified transfer.
   *
   * @param client state transition client
   * @param context verification context
   * @param tokenBytes serialized token bytes
   * @param recipient recipient address
   * @param signingService sender signing service
   *
   * @return transferred token
   *
   * @throws Exception when request or verification fails
   */
  public static Token transferToken(
          StateTransitionClient client,
          VerificationContext context,
          byte[] tokenBytes,
          Predicate recipient,
          SigningService signingService
  ) throws Exception {
    Token token = Token.fromCbor(tokenBytes);
    Assertions.assertEquals(VerificationStatus.OK, token.verify(context).getStatus());

    TransferTransaction transaction = TransferTransaction.create(token, recipient, StateMask.generate(), null);

    return TokenUtils.transferToken(
            client,
            context,
            token,
            transaction,
            SignaturePredicateUnlockScript.create(transaction, signingService)
    );
  }

  /**
   * Submit a prepared transfer transaction and return resulting transferred token.
   *
   * @param client state transition client
   * @param context verification context
   * @param token source token
   * @param transaction transfer transaction
   * @param unlockScript unlock script for transaction
   *
   * @return transferred token
   *
   * @throws Exception when request or verification fails
   */
  public static Token transferToken(
          StateTransitionClient client,
          VerificationContext context,
          Token token,
          TransferTransaction transaction,
          UnlockScript unlockScript
  ) throws Exception {
    CertificationResponse response = client.submitCertificationRequest(
            CertificationData.fromTransaction(transaction, unlockScript)
    ).get();

    if (response.getStatus() != CertificationStatus.SUCCESS) {
      throw new RuntimeException(
              String.format("Certification Request failed with status '%s'", response.getStatus()));
    }

    return token.transfer(
            transaction.toCertifiedTransaction(
                    context.getTrustBase(),
                    context.getPredicateVerifier(),
                    InclusionProofUtils.waitInclusionProof(
                            client, context.getTrustBase(), context.getPredicateVerifier(),
                            transaction).get()
            ),
            context
    );
  }

}
