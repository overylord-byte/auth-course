package com.course.auth.security.csrf;

import com.course.auth.domain.SessionData;
import com.course.auth.service.CsrfTokenService;
import com.course.auth.service.SessionStore;
import com.course.auth.web.SessionCookieManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

import static com.course.auth.api.AuthenticationApi.LOGIN_PATH;
import static com.course.auth.api.CsrfApi.CSRF_HEADER_NAME;

@Component
@RequiredArgsConstructor
public class CsrfProtectionFilter extends OncePerRequestFilter {
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final CsrfTokenService csrfTokenService;
    private final SessionStore sessionStore;
    private final SessionCookieManager sessionCookieManager;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        //Scope
        if (SAFE_METHODS.contains(request.getMethod())) {
            return true;
        }

        // Other exceptions
        if (Objects.equals(request.getServletPath(), LOGIN_PATH)) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> sessionId = sessionCookieManager.readSessionId(request);
        if (sessionId.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<SessionData> sessionData = sessionStore.getSession(sessionId.get());
        if (sessionData.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String expectedCsrfToken = sessionData.get().csrfToken();
        String actualCsrfToken = request.getHeader(CSRF_HEADER_NAME);
        if (!csrfTokenService.matches(expectedCsrfToken, actualCsrfToken)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
