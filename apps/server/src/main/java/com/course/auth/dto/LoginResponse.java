package com.course.auth.dto;

public record LoginResponse(boolean authenticated, String username) {
}
