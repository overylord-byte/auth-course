package com.course.auth.dto;

public record SessionResponse(boolean authenticated, String username) {
}
