package com.reddiax.rdxvideo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for accessing security-related information from the current JWT token.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Get the current JWT token from the security context.
     */
    public static Optional<Jwt> getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    /**
     * Get the current user ID (subject claim from JWT).
     */
    public static Optional<String> getCurrentUserId() {
        return getCurrentJwt().map(Jwt::getSubject);
    }

    /**
     * Get the current user's email.
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentJwt().map(jwt -> jwt.getClaimAsString("email"));
    }

    /**
     * Get the current user's given name (first name).
     */
    public static Optional<String> getCurrentUserGivenName() {
        return getCurrentJwt().map(jwt -> jwt.getClaimAsString("given_name"));
    }

    /**
     * Get the current user's family name (last name).
     */
    public static Optional<String> getCurrentUserFamilyName() {
        return getCurrentJwt().map(jwt -> jwt.getClaimAsString("family_name"));
    }

    /**
     * Get the current user's full name.
     */
    public static Optional<String> getCurrentUserFullName() {
        return getCurrentJwt().map(jwt -> {
            String givenName = jwt.getClaimAsString("given_name");
            String familyName = jwt.getClaimAsString("family_name");
            if (givenName != null && familyName != null) {
                return givenName + " " + familyName;
            } else if (givenName != null) {
                return givenName;
            } else if (familyName != null) {
                return familyName;
            }
            return null;
        });
    }

    /**
     * Get the current user's roles.
     */
    @SuppressWarnings("unchecked")
    public static List<String> getCurrentUserRoles() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsStringList("roles"))
                .orElse(Collections.emptyList());
    }

    /**
     * Check if the current user has a specific role.
     */
    public static boolean hasRole(String role) {
        return getCurrentUserRoles().contains(role);
    }

    /**
     * Check if the user is authenticated.
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Get the client ID (azp/authorized party) from the JWT token.
     * This identifies which application made the request (e.g., 'rdx-video-cms' or 'rdx-glance').
     */
    public static Optional<String> getClientId() {
        return getCurrentJwt().map(jwt -> {
            // Try 'azp' (authorized party) first, then 'client_id'
            String clientId = jwt.getClaimAsString("azp");
            if (clientId == null) {
                clientId = jwt.getClaimAsString("client_id");
            }
            return clientId;
        });
    }

    /**
     * Check if the request is coming from the CMS application.
     */
    public static boolean isFromCMS() {
        return getClientId()
                .map(clientId -> "rdx-video-cms".equals(clientId))
                .orElse(false);
    }

    /**
     * Check if the request is coming from a mobile application.
     */
    public static boolean isFromMobileApp() {
        return getClientId()
                .map(clientId -> "rdx-glance".equals(clientId) || "rdx-video-player".equals(clientId))
                .orElse(false);
    }
}
