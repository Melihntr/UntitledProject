package com.project.common.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
public class UserHeaderWebConfiguration implements WebMvcConfigurer {

    private final UserHeaderInterceptor userHeaderInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT security supersedes the legacy X-User-Id interceptor.
    }
}
