package com.project.common.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class UserHeaderWebConfigurationTest {

    @Test
    void addInterceptorsDoesNotRegisterLegacyHeaderInterceptor() {
        UserHeaderInterceptor interceptor = new UserHeaderInterceptor();
        UserHeaderWebConfiguration configuration = new UserHeaderWebConfiguration(interceptor);
        InterceptorRegistry registry = mock(InterceptorRegistry.class);

        configuration.addInterceptors(registry);

        verifyNoInteractions(registry);
    }
}
