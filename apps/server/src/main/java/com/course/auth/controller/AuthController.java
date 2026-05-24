package com.course.auth.controller;

import com.course.auth.dto.JwtLoginResponse;
import com.course.auth.dto.LoginRequest;
import com.course.auth.service.JwtAuthenticationResult;
import com.course.auth.service.JwtAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    public static final String LOGIN_PATH = "/api/v1/auth/login";

    private final JwtAuthenticationService jwtAuthenticationService;

    @PostMapping(LOGIN_PATH)
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        JwtAuthenticationResult result =
                jwtAuthenticationService.login(request.username(), request.password());

        if (!result.authenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String accessToken = jwtAuthenticationService.createAccessToken(result.username());

        return ResponseEntity.ok(new JwtLoginResponse(
                accessToken,
                JwtAuthenticationService.TOKEN_TYPE,
                JwtAuthenticationService.TOKEN_EXPIRATION_SECONDS));
    }
}
