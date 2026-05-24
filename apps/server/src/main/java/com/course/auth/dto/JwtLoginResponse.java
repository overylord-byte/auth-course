package com.course.auth.dto;

public record JwtLoginResponse(String accessToken, String tokenType, long expiresIn) {
}
