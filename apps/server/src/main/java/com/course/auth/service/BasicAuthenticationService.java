package com.course.auth.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class BasicAuthenticationService {

    private static final String BASIC_PREFIX = "Basic ";

    private final Map<String, String> users = Map.of(
            "user", "password",
            "alex", "secret"
    );

    public BasicAuthenticationResult authenticate(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BASIC_PREFIX)) {
            return BasicAuthenticationResult.unauthenticated();
        }

        try {
            // RFC 7617 section 2: credentials are sent as Base64(user-id ":" password)
            // where the original bytes are interpreted with a charset; we use UTF-8.
            String base64Credentials = authorizationHeader.substring(BASIC_PREFIX.length());
            String decodedCredentials = new String(
                    Base64.getDecoder().decode(base64Credentials),
                    StandardCharsets.UTF_8);
            // Split only by the first ":" because password may contain additional ":" symbols.
            String[] parts = decodedCredentials.split(":", 2);
            if (parts.length != 2) {
                return BasicAuthenticationResult.unauthenticated();
            }

            String username = parts[0];
            String password = parts[1];

            String expectedPassword = users.get(username);

            if (expectedPassword == null || !expectedPassword.equals(password)) {
                return BasicAuthenticationResult.unauthenticated();
            }

            // Base64 is just an encoding transport format, not encryption.
            // Basic auth must be used over HTTPS, otherwise credentials are exposed in transit.
            return BasicAuthenticationResult.authenticated(username);
        } catch (IllegalArgumentException e) {
            return BasicAuthenticationResult.unauthenticated();
        }
    }
}
