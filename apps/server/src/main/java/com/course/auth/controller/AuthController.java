package com.course.auth.controller;

import com.course.auth.dto.LoginRequest;
import com.course.auth.dto.LoginResponse;
import com.course.auth.dto.LogoutResponse;
import com.course.auth.service.SessionAuthenticationResult;
import com.course.auth.service.SessionAuthenticationService;
import com.course.auth.service.SessionStore;
import com.course.auth.web.SessionCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
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

    //TODO: should be removed later
    public static final String SESSION_COOKIE_NAME = "SESSION_ID";

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    //TODO: should be removed later
    private final SessionCookieManager sessionCookieManager;

    private final SessionStore sessionStore;

    @PostMapping(LOGIN_PATH)
    public ResponseEntity<?> login(
            @RequestBody LoginRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(body.username(), body.password());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(token);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
        securityContextRepository.saveContext(new SecurityContextImpl(authentication), request, response);

        return ResponseEntity.ok()
                .body(new LoginResponse(true, authentication.getName()));
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
