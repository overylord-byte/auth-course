package com.course.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicAuthenticationServiceTest {

    private BasicAuthenticationService basicAuthenticationService;

    @BeforeEach
    void setUp() {
        basicAuthenticationService = new BasicAuthenticationService();
    }

    @Test
    void shouldReturnUnauthenticatedWhenAuthorizationHeaderIsMissing() {
        BasicAuthenticationResult result = basicAuthenticationService.authenticate(null);

        assertFalse(result.authenticated());
        assertNull(result.username());
    }

    @Test
    void shouldReturnUnauthenticatedWhenSchemeIsNotBasic() {
        BasicAuthenticationResult result = basicAuthenticationService.authenticate("Bearer some-token");

        assertFalse(result.authenticated());
        assertNull(result.username());
    }

    @Test
    void shouldReturnUnauthenticatedWhenBase64IsInvalid() {
        BasicAuthenticationResult result = basicAuthenticationService.authenticate("Basic !!!");

        assertFalse(result.authenticated());
        assertNull(result.username());
    }

    @Test
    void shouldReturnUnauthenticatedWhenDecodedCredentialsDoNotContainColon() {
        String malformed = base64("userpassword");

        BasicAuthenticationResult result = basicAuthenticationService.authenticate("Basic " + malformed);

        assertFalse(result.authenticated());
        assertNull(result.username());
    }

    @Test
    void shouldReturnUnauthenticatedWhenUsernameIsUnknown() {
        String unknownUser = base64("unknown:password");

        BasicAuthenticationResult result = basicAuthenticationService.authenticate("Basic " + unknownUser);

        assertFalse(result.authenticated());
        assertNull(result.username());
    }

    @Test
    void shouldReturnUnauthenticatedWhenPasswordIsIncorrect() {
        String wrongPassword = base64("user:wrong");

        BasicAuthenticationResult result = basicAuthenticationService.authenticate("Basic " + wrongPassword);

        assertFalse(result.authenticated());
        assertNull(result.username());
    }

    @Test
    void shouldReturnAuthenticatedWhenCredentialsAreValid() {
        String validCredentials = base64("user:password");

        BasicAuthenticationResult result = basicAuthenticationService.authenticate("Basic " + validCredentials);

        assertTrue(result.authenticated());
        assertEquals("user", result.username());
    }

    private String base64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
