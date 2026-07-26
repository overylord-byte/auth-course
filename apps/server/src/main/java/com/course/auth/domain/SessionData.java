package com.course.auth.domain;

import java.time.Instant;

public record SessionData(String username, Instant expiresAt, String csrfToken) {
}
