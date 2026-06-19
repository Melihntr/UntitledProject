package com.project.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserHeaderWebConfigurationTest {

    @Test
    void addInterceptorsRegistersApiInterceptor() {
        UserHeaderInterceptor interceptor = new UserHeaderInterceptor();
        UserHeaderWebConfiguration configuration = new UserHeaderWebConfiguration(interceptor);
        InterceptorRegistry registry = mock(InterceptorRegistry.class);
        org.springframework.web.servlet.config.annotation.InterceptorRegistration registration =
                mock(org.springframework.web.servlet.config.annotation.InterceptorRegistration.class);
        when(registry.addInterceptor(interceptor)).thenReturn(registration);

        configuration.addInterceptors(registry);

        verify(registration).addPathPatterns("/api/**");
    }
}
