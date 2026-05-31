package com.course.auth.domain;

public record JwtTokenPayload(String sub, String type, long iat, long exp) {
}
