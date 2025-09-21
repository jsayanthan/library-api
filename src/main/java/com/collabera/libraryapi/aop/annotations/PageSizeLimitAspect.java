package com.collabera.libraryapi.aop.annotations;

import com.collabera.libraryapi.core.constants.PaginationConstants;
import com.collabera.libraryapi.web.exception.PageSizeLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class PageSizeLimitAspect {
    private final HttpServletRequest request;

    @Around("@annotation(limit)")
    public Object enforceLimit(ProceedingJoinPoint pjp, PageSizeLimit limit) throws Throwable {
        var raw = request.getParameter(limit.param());
        if (raw != null) {
            try {
                int size = Integer.parseInt(raw);
                int ceiling = Math.min(limit.max(), PaginationConstants.MAX_SIZE);
                if (size > ceiling) {
                    throw new PageSizeLimitExceededException(
                            "Requested size=" + size + " exceeds max allowed " + ceiling);
                }
            } catch (NumberFormatException nfe) {
                throw new PageSizeLimitExceededException("Invalid page size value");
            }
        }
        return pjp.proceed();
    }
}
