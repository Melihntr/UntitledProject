package com.project.transaction.api.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class SecurityHeaderAspect {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ID_MDC_KEY = "userId";

    @Around("execution(* com.project.transaction.api.controller..*(..))")
    public Object validateUserHeader(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String userId = request.getHeader(USER_ID_HEADER);

        if (userId == null || userId.isBlank()) {
            log.warn("security.header.missing header={}", USER_ID_HEADER);
            throw new SecurityException("Missing mandatory security header: " + USER_ID_HEADER);
        }

        MDC.put(USER_ID_MDC_KEY, userId);
        try {
            return joinPoint.proceed();
        } finally {
            MDC.remove(USER_ID_MDC_KEY);
        }
    }
}
