package com.course.auth.service;

public record SessionAuthenticationResult(boolean authenticated, String username, String sessionId) {

    public static SessionAuthenticationResult authenticated(String username, String sessionId) {
        return new SessionAuthenticationResult(true, username, sessionId);
    }

    public static SessionAuthenticationResult unauthenticated() {
        return new SessionAuthenticationResult(false, null, null);
    }
}
