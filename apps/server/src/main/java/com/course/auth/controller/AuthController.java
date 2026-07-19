package com.course.auth.controller;

import com.course.auth.dto.LoginRequest;
import com.course.auth.dto.LoginResponse;
import com.course.auth.dto.LogoutResponse;
import com.course.auth.service.SessionAuthenticationResult;
import com.course.auth.service.SessionAuthenticationService;
import com.course.auth.service.SessionStore;
import com.course.auth.web.SessionCookieManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class AuthController {

    public static final String LOGIN_PATH = "/api/v1/auth/login";
    public static final String LOGOUT_PATH = "/api/v1/auth/logout";
    public static final String SESSION_COOKIE_NAME = "SESSION_ID";

    private final SessionAuthenticationService sessionAuthenticationService;
    private final SessionCookieManager sessionCookieManager;
    private final SessionStore sessionStore;

    @PostMapping(LOGIN_PATH)
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        SessionAuthenticationResult result =
                sessionAuthenticationService.login(request.username(), request.password());

        if (!result.authenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        ResponseCookie cookie = sessionCookieManager.create(result.sessionId());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(true, result.username()));
    }

    @PostMapping(LOGOUT_PATH)
    public ResponseEntity<LogoutResponse> logout(
            @CookieValue(value = SESSION_COOKIE_NAME, required = false) String sessionId) {
        if (sessionId != null) {
            sessionStore.deleteSession(sessionId);
        }

        ResponseCookie cookie = sessionCookieManager.clear();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LogoutResponse(true));
    }
}
