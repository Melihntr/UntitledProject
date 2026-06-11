package com.project.user.domain.usecase;

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
        log.info("Deleting user record with ID: {}", userId);
        userPort.deleteUserById(userId);
        log.info("Successfully deleted user record with ID: {}", userId);
    }
}
