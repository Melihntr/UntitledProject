package com.project.common.security;

import com.project.common.exception.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class UserHeaderInterceptor implements HandlerInterceptor {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ID_ATTRIBUTE = CurrentUserProvider.class.getName() + ".userId";
    private static final String USER_ID_MDC_KEY = "userId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!HttpMethod.GET.matches(request.getMethod())) {
            return true;
        }

        String userId = request.getHeader(USER_ID_HEADER);
        if (!StringUtils.hasText(userId)) {
            log.warn("security.header.missing header={} method={} path={}",
                    USER_ID_HEADER, request.getMethod(), request.getRequestURI());
            throw new AccessDeniedException("Missing mandatory security header: " + USER_ID_HEADER);
        }

        request.setAttribute(USER_ID_ATTRIBUTE, userId);
        MDC.put(USER_ID_MDC_KEY, userId);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        MDC.remove(USER_ID_MDC_KEY);
    }
}
