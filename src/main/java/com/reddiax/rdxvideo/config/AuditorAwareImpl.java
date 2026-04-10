package com.reddiax.rdxvideo.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementation of AuditorAware to extract the current user from the JWT token.
 * This is used by JPA auditing to populate createdBy and modifiedBy fields.
 */
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("system");
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getSubject());
        }
        
        if ("anonymousUser".equals(principal)) {
            return Optional.of("anonymous");
        }

        return Optional.of(principal.toString());
    }
}
