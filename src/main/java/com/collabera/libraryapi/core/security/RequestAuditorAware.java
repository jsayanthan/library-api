package com.collabera.libraryapi.core.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
public class RequestAuditorAware implements AuditorAware<String> {
    private static final String DEFAULT_AUDITOR = "system";
    private static final String HEADER = "X-User";

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                var header = sra.getRequest().getHeader(HEADER);
                if (header != null && !header.isBlank()) return Optional.of(header);
            }
        } catch (Exception ignored) { }
        return Optional.of(DEFAULT_AUDITOR);
    }
}
