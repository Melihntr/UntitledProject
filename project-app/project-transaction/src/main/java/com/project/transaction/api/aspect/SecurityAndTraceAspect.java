package com.project.transaction.api.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Aspect class responsible for intercepting API requests to validate security headers
 * and manage Trace IDs, ensuring end-to-end request traceability across the system.
 */
@Aspect
@Component
public class SecurityAndTraceAspect {

    private static final Logger logger = LoggerFactory.getLogger(SecurityAndTraceAspect.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String USER_ID_HEADER = "X-User-Id";

    /**
     * This pointcut intercepts all public methods within the API controller package.
     * It enforces security rules and injects diagnostic context before the controller executes.
     *
     * @param joinPoint The execution point of the intercepted method.
     * @return The result of the target method execution.
     * @throws Throwable If an error occurs during execution or security validation fails.
     */
    @Around("execution(* com.project.transaction.api.controller..*(..))")
    public Object handleSecurityAndTrace(ProceedingJoinPoint joinPoint) throws Throwable {
        
        // 1. Intercept the incoming HTTP Request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();

        // 2. Security Check: Verify the presence of the mandatory X-User-Id header
        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.trim().isEmpty()) {
            logger.error("Security Breach Attempt: Missing X-User-Id in header");
            // Note: In a production environment, a custom UnauthorizedException or BusinessException is preferred.
            throw new SecurityException("Missing mandatory security header: " + USER_ID_HEADER);
        }

        // 3. Traceability Check: Retrieve the TraceID from the header, or generate a new one if missing
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString();
            logger.info("No Trace-Id found in header. Generated new Trace-Id: {}", traceId);
        }

        // 4. Inject the TraceID and UserID into the MDC (Mapped Diagnostic Context)
        // This ensures that all subsequent logs for this specific thread will automatically include these identifiers.
        MDC.put("traceId", traceId);
        MDC.put("userId", userId);

        try {
            logger.info("Starting execution of endpoint: {}", request.getRequestURI());
            
            // Proceed with the execution of the target Controller method
            Object result = joinPoint.proceed();
            
            logger.info("Successfully executed endpoint: {}", request.getRequestURI());
            return result;
            
        } finally {
            // Clear the MDC context after execution to prevent memory leaks within the application's thread pool
            MDC.clear();
        }
    }
}