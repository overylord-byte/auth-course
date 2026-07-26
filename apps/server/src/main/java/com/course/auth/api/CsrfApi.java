package com.course.auth.api;

public class CsrfApi {
    private static final String AUTH_PATH = "/api/v1/auth";

    public static final String CSRF_PATH = AUTH_PATH + "/csrf";
    public static final String CSRF_HEADER_NAME = "X-CSRF-TOKEN";
    public static final String CSRF_PARAMETER_NAME = "_csrf";
}
