package com.conspiracy.forum.util;

import com.conspiracy.forum.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for common authentication operations.
 */
public final class SecurityUtils {

    private static final String ANONYMOUS_USER = "anonymousUser";

    private SecurityUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets the username of the currently authenticated user.
     *
     * @return the username of the authenticated user
     * @throws UnauthorizedException if the user is not authenticated
     */
    public static String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isAnonymousOrUnauthenticated(authentication)) {
            throw new UnauthorizedException("You must be logged in to perform this action");
        }
        return authentication.getName();
    }

    /**
     * Checks if the current authentication represents an anonymous or unauthenticated user.
     *
     * @param authentication the authentication object
     * @return true if the user is anonymous or unauthenticated
     */
    private static boolean isAnonymousOrUnauthenticated(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }
        Object principal = authentication.getPrincipal();
        return principal instanceof String && ANONYMOUS_USER.equals(principal);
    }
}
