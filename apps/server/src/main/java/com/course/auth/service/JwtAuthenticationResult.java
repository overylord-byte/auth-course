package com.course.auth.service;

public record JwtAuthenticationResult(boolean authenticated, String username) {

    public static JwtAuthenticationResult authenticated(String username) {
        return new JwtAuthenticationResult(true, username);
    }

    public static JwtAuthenticationResult unauthenticated() {
        return new JwtAuthenticationResult(false, null);
    }
}
