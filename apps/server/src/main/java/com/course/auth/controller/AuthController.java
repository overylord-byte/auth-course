package com.course.auth.controller;

import com.course.auth.dto.JwtLoginResponse;
import com.course.auth.dto.LoginRequest;
import com.course.auth.dto.LogoutResponse;
import com.course.auth.service.JwtAuthenticationResult;
import com.course.auth.service.JwtAuthenticationService;
import com.course.auth.service.JwtTokenService;
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

    public static final String ACCESS_TOKEN_PATH = "/api/v1/auth/accessToken";
    public static final String REFRESH_PATH = "/api/v1/auth/refresh";
    public static final String LOGOUT_PATH = "/api/v1/auth/logout";
    public static final String REFRESH_COOKIE_NAME = "REFRESH_TOKEN";

    private final JwtAuthenticationService jwtAuthenticationService;
    private final JwtTokenService jwtTokenService;

    @PostMapping(ACCESS_TOKEN_PATH)
    public ResponseEntity<?> accessToken(@RequestBody LoginRequest request) {
        JwtAuthenticationResult result =
                jwtAuthenticationService.login(request.username(), request.password());

        if (!result.authenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String accessToken = jwtTokenService.createAccessToken(result.username());
        String refreshToken = jwtTokenService.createRefreshToken(result.username());

        // Secure must be enabled in production when HTTPS is used.
        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofSeconds(JwtTokenService.REFRESH_TOKEN_EXPIRATION_SECONDS))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new JwtLoginResponse(
                        accessToken,
                        JwtAuthenticationService.TOKEN_TYPE,
                        JwtTokenService.ACCESS_TOKEN_EXPIRATION_SECONDS));
    }

    @PostMapping(REFRESH_PATH)
    public ResponseEntity<?> refresh(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        JwtAuthenticationResult result = jwtAuthenticationService.refresh(refreshToken);

        if (!result.authenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        return ResponseEntity.ok(new JwtLoginResponse(
                result.accessToken(),
                JwtAuthenticationService.TOKEN_TYPE,
                JwtTokenService.ACCESS_TOKEN_EXPIRATION_SECONDS));
    }

    @PostMapping(LOGOUT_PATH)
    public ResponseEntity<LogoutResponse> logout() {
        // Secure must be enabled in production when HTTPS is used.
        ResponseCookie clearedCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearedCookie.toString())
                .body(new LogoutResponse(true));
    }
}
