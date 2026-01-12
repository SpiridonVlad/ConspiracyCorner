package com.conspiracy.forum.util;

import com.conspiracy.forum.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private static final String ANONYMOUS_USER = "anonymousUser";

    private SecurityUtils() {
    }

    public static String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isAnonymousOrUnauthenticated(authentication)) {
            throw new UnauthorizedException("You must be logged in to perform this action");
        }
        return authentication.getName();
    }

    private static boolean isAnonymousOrUnauthenticated(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }
        Object principal = authentication.getPrincipal();
        return principal instanceof String && ANONYMOUS_USER.equals(principal);
    }
}
