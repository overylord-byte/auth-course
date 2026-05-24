package com.course.auth.domain;

public record JwtTokenPayload(String sub, long iat, long exp) {
}
