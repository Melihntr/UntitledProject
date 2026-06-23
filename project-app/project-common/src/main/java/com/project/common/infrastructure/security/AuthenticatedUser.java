package com.project.common.infrastructure.security;

public record AuthenticatedUser(String userId, String role) {
}
