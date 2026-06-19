package com.project.common.security;

import com.project.common.exception.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CurrentUserProvider {

    public String getUserId() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            throw new AccessDeniedException("Current request user is not available.");
        }

        HttpServletRequest request = attributes.getRequest();
        Object userId = request.getAttribute(UserHeaderInterceptor.USER_ID_ATTRIBUTE);
        if (!(userId instanceof String value) || value.isBlank()) {
            throw new AccessDeniedException("Current request user is not available.");
        }
        return value;
    }
}
