package com.course.auth.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JwtUtil {
    private static final String HMAC_SHA256 = "HmacSHA256";

    public static String base64UrlEncode(String message) {
        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(message.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64UrlDecode(String encoded) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encoded);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    public static String sign(String message, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);

            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    HMAC_SHA256);

            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(signatureBytes);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create HMAC SHA-256 signature", exception);
        }
    }

    public static boolean verify(String message, String signature, String secret) {
        String calculatedSignature = sign(message, secret);
        return calculatedSignature.equals(signature);
    }
}
