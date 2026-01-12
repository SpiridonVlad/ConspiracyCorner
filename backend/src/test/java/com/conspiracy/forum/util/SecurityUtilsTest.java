package com.conspiracy.forum.util;

import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.exception.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAuthenticatedUsername_ShouldReturnUsername_WhenAuthenticated() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        String username = SecurityUtils.getAuthenticatedUsername();

        assertEquals("testuser", username);
    }

    @Test
    void getAuthenticatedUsername_ShouldThrow_WhenNotAuthenticated() {
        SecurityContextHolder.clearContext();

        assertThrows(UnauthorizedException.class, SecurityUtils::getAuthenticatedUsername);
    }

    @Test
    void getAuthenticatedUsername_ShouldThrow_WhenAnonymousUser() {
        UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken("anonymousUser", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(UnauthorizedException.class, SecurityUtils::getAuthenticatedUsername);
    }

    @Test
    void getAuthenticatedUsername_ShouldThrow_WhenAuthenticationNotAuthenticated() {
        UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
        auth.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(UnauthorizedException.class, SecurityUtils::getAuthenticatedUsername);
    }
}
