package com.wayos.connector.line;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class LINESignatureValidator {
    private static final String HASH_ALGORITHM = "HmacSHA256";
    private final SecretKeySpec secretKeySpec;

    /**
     * Create new instance with channel secret.
     */
    public LINESignatureValidator(byte[] channelSecret) {
        this.secretKeySpec = new SecretKeySpec(channelSecret, HASH_ALGORITHM);
    }

    /**
     * Validate signature.
     *
     * @param content Body of the http request in byte array.
     * @param headerSignature Signature value from `X-LINE-Signature` HTTP header
     *
     * @return True if headerSignature matches signature of the content. False otherwise.
     */
    public boolean validateSignature(byte[] content, String headerSignature) {
        final byte[] signature = generateSignature(content);
        final byte[] decodeHeaderSignature = Base64.getDecoder().decode(headerSignature);
        return MessageDigest.isEqual(decodeHeaderSignature, signature);
    }

    /**
     * Generate signature value.
     *
     * @param content Body of the http request.
     *
     * @return generated signature value.
     */
    public byte [] generateSignature(byte[] content) {
        try {
            Mac mac = Mac.getInstance(HASH_ALGORITHM);
            mac.init(secretKeySpec);
            return mac.doFinal(content);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // "HmacSHA256" is always supported in Java 8 platform.
            //   (see https://docs.oracle.com/javase/8/docs/api/javax/crypto/Mac.html)
            // All valid-SecretKeySpec-instance are not InvalidKey.
            //   (because the key for HmacSHA256 can be of any length. see RFC2104)
            throw new IllegalStateException(e);
        }
    }

}