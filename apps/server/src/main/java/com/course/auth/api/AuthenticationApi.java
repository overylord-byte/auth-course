package com.course.auth.api;

public final class AuthenticationApi {
    private static final String AUTH_PATH = "/api/v1/auth";

    public static final String LOGIN_PATH = AUTH_PATH + "/login";
    public static final String LOGOUT_PATH = AUTH_PATH + "/logout";


    private AuthenticationApi() {
    }
}
