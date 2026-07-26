package com.course.auth.controller;

import com.course.auth.dto.CsrfTokenResponse;
import com.course.auth.service.SessionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.course.auth.api.CsrfApi.CSRF_PATH;
import static com.course.auth.web.SessionCookieManager.SESSION_COOKIE_NAME;

@RestController
@RequiredArgsConstructor
public class CsrfController {
    private final SessionStore sessionStore;

    @GetMapping(CSRF_PATH)
    public ResponseEntity<CsrfTokenResponse> getCsrfToken(
            @CookieValue(value = SESSION_COOKIE_NAME, required = false) String sessionId
    ) {
        return sessionStore
                .getSession(sessionId)
                .map(session -> ResponseEntity.ok(
                                new CsrfTokenResponse(session.csrfToken())
                        )
                )
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
