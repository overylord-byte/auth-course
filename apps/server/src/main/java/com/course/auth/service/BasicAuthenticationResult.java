package com.course.auth.service;

public record BasicAuthenticationResult(boolean authenticated, String username) {

    public static BasicAuthenticationResult authenticated(String username) {
        return new BasicAuthenticationResult(true, username);
    }

    public static BasicAuthenticationResult unauthenticated() {
        return new BasicAuthenticationResult(false, null);
    }
}
