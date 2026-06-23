package com.project.user.infrastructure.bootstrap;

import com.project.common.infrastructure.tracing.TraceIdProvider;
import com.project.user.domain.usecase.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.handler.CreateUserHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Bootstraps initial data for the application on startup.
 * Creates default test users and triggers the wallet creation events
 * to facilitate easy testing with the H2 in-memory database.
 */
@Component
@Order(2)
@Slf4j
@RequiredArgsConstructor
public class UserDataSeeder implements CommandLineRunner {

    private static final String TRACE_ID_MDC_KEY = "traceId";

    private final CreateUserHandler createUserHandler;
    private final TraceIdProvider traceIdProvider;

    /**
     * Executes automatically after the Spring context is fully initialized.
     *
     * @param args Command line arguments
     */
    @Override
    public void run(String... args) {
        String previousTraceId = MDC.get(TRACE_ID_MDC_KEY);
        String seedTraceId = traceIdProvider.currentTraceIdOrNew();
        log.info("seed.user-data.request");

        try {
            UserModel user1 = seedUser(
                    "test_sender",
                    "sender@enterprise.com"
            );
            UserModel user2 = seedUser(
                    "test_receiver",
                    "receiver@enterprise.com"
            );

            log.info("seed.user-data.success traceId={} senderUserId={} receiverUserId={}",
                    seedTraceId, user1.getId(), user2.getId());
        } finally {
            restoreTraceId(previousTraceId);
        }
    }

    private UserModel seedUser(String username, String email) {
        UserCreateInput input = UserCreateInput.builder()
                .username(username)
                .email(email)
                .rawPassword("TestPass123!")
                .build();
        return createUserHandler.handle(input);
    }

    private void restoreTraceId(String previousTraceId) {
        if (previousTraceId == null) {
            MDC.remove(TRACE_ID_MDC_KEY);
        } else {
            MDC.put(TRACE_ID_MDC_KEY, previousTraceId);
        }
    }
}
