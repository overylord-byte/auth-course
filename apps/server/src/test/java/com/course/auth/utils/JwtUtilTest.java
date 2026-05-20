package com.course.auth.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "my-course-secret-key";

    @Test
    void base64UrlEncodeShouldEncodeWithoutPaddingOrStandardBase64Chars() {
        String jsonHeader = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

        String encoded = JwtUtil.base64UrlEncode(jsonHeader);

        // Base64Url is an encoding format for transport, not encryption.
        // Anyone can decode the payload and read the data.
        assertFalse(encoded.contains("="), "Base64Url must not use padding");
        assertFalse(encoded.contains("+"), "Base64Url must not use '+'");
        assertFalse(encoded.contains("/"), "Base64Url must not use '/'");
    }

    @Test
    void signShouldCreateDeterministicSignatureForSameMessageAndSecret() {
        String message = "{\"sub\":\"user\",\"role\":\"USER\"}";

        String firstSignature = JwtUtil.sign(message, SECRET);
        String secondSignature = JwtUtil.sign(message, SECRET);

        assertEquals(firstSignature, secondSignature);
    }

    @Test
    void signShouldCreateDifferentSignatureWhenMessageChanges() {
        String originalMessage = "{\"sub\":\"user\",\"role\":\"USER\"}";
        String modifiedMessage = "{\"sub\":\"user\",\"role\":\"ADMIN\"}";

        String originalSignature = JwtUtil.sign(originalMessage, SECRET);
        String modifiedSignature = JwtUtil.sign(modifiedMessage, SECRET);

        assertNotEquals(originalSignature, modifiedSignature);
    }

    @Test
    void shouldCreateDifferentSignatureForDifferentSecret() {
        String message = "{\"sub\":\"user\",\"role\":\"USER\"}";
        String otherSecret = "another-secret-key";

        String signatureWithOriginalSecret = JwtUtil.sign(message, SECRET);
        String signatureWithOtherSecret = JwtUtil.sign(message, otherSecret);

        assertNotEquals(signatureWithOriginalSecret, signatureWithOtherSecret);
    }

    @Test
    void shouldVerifyOriginalMessageSignature() {
        String message = "{\"sub\":\"user\",\"role\":\"USER\"}";
        String signature = JwtUtil.sign(message, SECRET);

        boolean isValid = JwtUtil.verify(message, signature, SECRET);

        assertTrue(isValid);
    }

    @Test
    void shouldFailVerificationWhenPayloadIsModified() {
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String originalPayloadJson = "{\"sub\":\"user\",\"role\":\"USER\"}";
        String attackerPayloadJson = "{\"sub\":\"user\",\"role\":\"ADMIN\"}";

        String encodedHeader = JwtUtil.base64UrlEncode(headerJson);
        String encodedOriginalPayload = JwtUtil.base64UrlEncode(originalPayloadJson);
        String originalData = encodedHeader + "." + encodedOriginalPayload;

        String signature = JwtUtil.sign(originalData, SECRET);

        // The attacker changes the payload, for example role USER -> ADMIN,
        // but keeps the original signature. The signature protects data from tampering:
        // if the payload changes, verification must return false.
        String encodedAttackerPayload = JwtUtil.base64UrlEncode(attackerPayloadJson);
        String modifiedData = encodedHeader + "." + encodedAttackerPayload;

        boolean isValid = JwtUtil.verify(modifiedData, signature, SECRET);

        assertFalse(isValid);
    }

    @Test
    void shouldFailVerificationWhenWrongSecretIsUsed() {
        String message = "{\"sub\":\"user\",\"role\":\"USER\"}";
        String wrongSecret = "attacker-guessed-secret";

        String signature = JwtUtil.sign(message, SECRET);

        // The secret must be stored only on the backend.
        // Without the correct secret, the signature cannot be recalculated correctly,
        // so verification must fail.
        boolean isValid = JwtUtil.verify(message, signature, wrongSecret);

        assertFalse(isValid);
    }

    @Test
    void shouldFailVerificationForRandomSignature() {
        String message = "{\"sub\":\"user\",\"role\":\"USER\"}";
        String randomSignature = "totally-fake-signature-value";

        boolean isValid = JwtUtil.verify(message, randomSignature, SECRET);

        assertFalse(isValid);
    }
}