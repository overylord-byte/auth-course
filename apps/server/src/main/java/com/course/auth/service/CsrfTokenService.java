package com.course.auth.service;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CsrfTokenService {

    private static final int TOKEN_LENGTH_BYTES = 32;
    private final SecureRandom random = new SecureRandom();

    public String generateToken() {
        byte[] bytes = new byte[TOKEN_LENGTH_BYTES];
        random.nextBytes(bytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public boolean matches(String expectedToken, String actualToken) {
        if (expectedToken == null || actualToken == null)
            return false;

        return MessageDigest.isEqual(expectedToken.getBytes(), actualToken.getBytes());
    }
}
