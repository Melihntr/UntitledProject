package com.project.transaction.infrastructure.api.security;

import org.springframework.stereotype.Component;

@Component
public class TransactionAccessValidator {

    public void validateSender(String loggedInUserId, String senderUserId) {
        if (!loggedInUserId.equals(senderUserId)) {
            throw new IllegalArgumentException("Security Violation: You can only transfer money from your own wallet.");
        }
    }

    public void validateHistoryOwner(String loggedInUserId, String userId) {
        if (!loggedInUserId.equals(userId)) {
            throw new IllegalArgumentException("Security Violation: You are not authorized to view this transaction history.");
        }
    }

    public void validateAdminRole(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new SecurityException("Access Denied: Only administrators can access system-wide fraud reports.");
        }
    }
}
