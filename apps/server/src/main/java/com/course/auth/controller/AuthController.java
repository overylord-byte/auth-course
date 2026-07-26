package com.course.auth.controller;

import com.course.auth.api.AuthenticationApi;
import com.course.auth.dto.LoginRequest;
import com.course.auth.dto.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    @PostMapping(AuthenticationApi.LOGIN_PATH)
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
}
