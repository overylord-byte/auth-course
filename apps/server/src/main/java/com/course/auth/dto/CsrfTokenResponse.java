package com.course.auth.dto;

import static com.course.auth.api.CsrfApi.CSRF_HEADER_NAME;
import static com.course.auth.api.CsrfApi.CSRF_PARAMETER_NAME;

public record CsrfTokenResponse(String token, String headerName, String parameterName) {
    public CsrfTokenResponse(String token) {
        this(token, CSRF_HEADER_NAME, CSRF_PARAMETER_NAME);
    }
}
