package com.unicity.sdk.shared.signing;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

public class SigningService implements ISigningService {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public SigningService(byte[] privateKeyBytes) {
        try {
            KeyFactory kf = KeyFactory.getInstance("ECDSA", "BC");
            ECParameterSpec ecSpec = org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec("secp256k1");
            ECPrivateKeySpec keySpec = new ECPrivateKeySpec(new BigInteger(1, privateKeyBytes), ecSpec);
            this.privateKey = kf.generatePrivate(keySpec);

            ECPoint Q = ecSpec.getG().multiply(new BigInteger(1, privateKeyBytes));
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(Q, ecSpec);
            this.publicKey = kf.generatePublic(pubSpec);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ISignature sign(byte[] data) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
            signature.initSign(privateKey);
            signature.update(data);
            byte[] sigBytes = signature.sign();
            return new com.unicity.sdk.shared.signing.Signature(sigBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean verify(byte[] data, ISignature signature) {
        try {
            Signature verifier = Signature.getInstance("SHA256withECDSA", "BC");
            verifier.initVerify(publicKey);
            verifier.update(data);
            return verifier.verify(signature.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getPublicKey() {
        return ((org.bouncycastle.jce.interfaces.ECPublicKey) publicKey).getQ().getEncoded(true);
    }
}