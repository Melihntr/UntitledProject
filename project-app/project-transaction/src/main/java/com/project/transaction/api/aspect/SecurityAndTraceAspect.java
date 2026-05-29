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
 * Aspect class to intercept API requests, validate security headers, 
 * and manage Trace IDs for end-to-end request tracking.
 */
@Aspect
@Component
public class SecurityAndTraceAspect {

    private static final Logger logger = LoggerFactory.getLogger(SecurityAndTraceAspect.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String USER_ID_HEADER = "X-User-Id";

    // Bu pointcut, com.project.transaction.api.controller paketi altındaki tüm public metodları yakalar
    @Around("execution(* com.project.transaction.api.controller..*(..))")
    public Object handleSecurityAndTrace(ProceedingJoinPoint joinPoint) throws Throwable {
        
        // 1. Gelen HTTP Request'i yakala
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();

        // 2. Güvenlik Kontrolü: X-User-Id header'da var mı?
        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.trim().isEmpty()) {
            logger.error("Security Breach Attempt: Missing X-User-Id in header");
            throw new SecurityException("Missing mandatory security header: " + USER_ID_HEADER);
            // Not: Gerçek projede custom bir BaseBusinessException fırlatırız.
        }

        // 3. İzlenebilirlik (Traceability) Kontrolü: TraceID var mı? Yoksa biz üretelim.
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString();
            logger.info("No Trace-Id found in header. Generated new Trace-Id: {}", traceId);
        }

        // 4. TraceID'yi MDC (Mapped Diagnostic Context) içine koy
        // Bu sayede atılacak tüm loglarda otomatik olarak bu TraceID yazacak.
        MDC.put("traceId", traceId);
        MDC.put("userId", userId);

        try {
            logger.info("Starting execution of endpoint: {}", request.getRequestURI());
            
            // İşlemin Controller'a gitmesine izin ver (Devam et)
            Object result = joinPoint.proceed();
            
            logger.info("Successfully executed endpoint: {}", request.getRequestURI());
            return result;
            
        } finally {
            // İşlem bitince hafıza sızıntısını (memory leak) önlemek için MDC'yi temizle
            MDC.clear();
        }
    }
}