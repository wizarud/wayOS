package com.wayos.connector.facebook;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class FacebookSignatureValidator {
    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    private final SecretKeySpec secretKeySpec;

    public FacebookSignatureValidator(byte[] channelSecret) {
        this.secretKeySpec = new SecretKeySpec(channelSecret, HMAC_SHA1);
    }

    public FacebookSignatureValidator(byte[] channelSecret, String algo) {
        this.secretKeySpec = new SecretKeySpec(channelSecret, algo);
    }

    public boolean validateSignature(byte[] content, String headerSignature) {
        final String signature = generateSignature(content);
        final String expected = headerSignature.substring(5);

        return signature.equals(expected);
    }

    public String generateSignature(byte[] content) {
        try {
            final Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(secretKeySpec);
            return bytesToHexString(mac.doFinal(content));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String bytesToHexString(byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            final int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}