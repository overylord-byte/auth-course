package com.course.auth.service;

public record JwtAuthenticationResult(boolean authenticated, String username, String accessToken) {

    public static JwtAuthenticationResult authenticated(String username) {
        return new JwtAuthenticationResult(true, username, null);
    }

    public static JwtAuthenticationResult authenticatedWithToken(String username, String accessToken) {
        return new JwtAuthenticationResult(true, username, accessToken);
    }

    public static JwtAuthenticationResult unauthenticated() {
        return new JwtAuthenticationResult(false, null, null);
    }
}
