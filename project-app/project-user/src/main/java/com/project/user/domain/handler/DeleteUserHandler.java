package com.project.user.domain.handler;

import com.project.common.domain.exception.ResourceNotFoundException;
import com.project.user.domain.port.UserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeleteUserHandler {

    private final UserPort userPort;

    @Transactional
    public void handle(String userId) {
        log.info("user.delete.request userId={}", userId);
        if (!userPort.deleteUserById(userId)) {
            log.warn("user.delete.not-found userId={}", userId);
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        log.info("user.delete.success userId={}", userId);
    }
}
