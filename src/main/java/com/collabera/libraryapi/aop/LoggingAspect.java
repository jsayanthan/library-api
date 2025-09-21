package com.collabera.libraryapi.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    private static final long WARN_THRESHOLD_MS = 400;
    private static final long INFO_THRESHOLD_MS = 150;

    @Around("execution(public * com.collabera.libraryapi.web.controller..*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        var start = System.currentTimeMillis();
        try {
            var result = pjp.proceed();
            var took = System.currentTimeMillis() - start;
            if (took >= WARN_THRESHOLD_MS) {
                log.warn("HTTP {}.{} took {} ms",
                        pjp.getSignature().getDeclaringType().getSimpleName(),
                        pjp.getSignature().getName(), took);
            } else if (took >= INFO_THRESHOLD_MS) {
                log.info("HTTP {}.{} took {} ms",
                        pjp.getSignature().getDeclaringType().getSimpleName(),
                        pjp.getSignature().getName(), took);
            } else {
                log.debug("HTTP {}.{} took {} ms",
                        pjp.getSignature().getDeclaringType().getSimpleName(),
                        pjp.getSignature().getName(), took);
            }
            return result;
        } catch (Throwable t) {
            var took = System.currentTimeMillis() - start;
            log.error("HTTP {}.{} failed after {} ms: {}",
                    pjp.getSignature().getDeclaringType().getSimpleName(),
                    pjp.getSignature().getName(), took, t.toString());
            throw t;
        }
    }
}
