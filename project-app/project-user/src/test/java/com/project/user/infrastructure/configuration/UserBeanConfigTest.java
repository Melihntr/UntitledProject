package com.project.user.infrastructure.configuration;

import com.project.user.domain.port.UserPort;
import com.project.user.domain.usecase.CreateUserHandler;
import com.project.user.domain.usecase.GetBasicUsersHandler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for the UserBeanConfig configuration class.
 */
class UserBeanConfigTest {

    @Test
    void createUserHandler_shouldReturnCreateUserHandlerInstance() {
        // Arrange
        UserPort userPort = mock(UserPort.class);
        UserBeanConfig config = new UserBeanConfig();

        // Act
        CreateUserHandler handler = config.createUserHandler(userPort);

        // Assert
        assertThat(handler).isNotNull();
        assertThat(handler).isInstanceOf(CreateUserHandler.class);

        // Calling again creates another instance (method uses "new"), so ensure distinct instances are returned
        CreateUserHandler handler2 = config.createUserHandler(userPort);
        assertThat(handler2).isNotSameAs(handler);
    }

    @Test
    void getBasicUsersHandler_shouldReturnGetBasicUsersHandlerInstance() {
        // Arrange
        UserPort userPort = mock(UserPort.class);
        UserBeanConfig config = new UserBeanConfig();

        // Act
        GetBasicUsersHandler handler = config.getBasicUsersHandler(userPort);

        // Assert
        assertThat(handler).isNotNull();
        assertThat(handler).isInstanceOf(GetBasicUsersHandler.class);

        // Calling again creates another instance (method uses "new"), so ensure distinct instances are returned
        GetBasicUsersHandler handler2 = config.getBasicUsersHandler(userPort);
        assertThat(handler2).isNotSameAs(handler);
    }
}
