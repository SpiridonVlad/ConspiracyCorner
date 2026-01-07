package com.conspiracy.forum.service;

import com.conspiracy.forum.dto.AuthResponse;
import com.conspiracy.forum.dto.LoginRequest;
import com.conspiracy.forum.dto.RegisterRequest;
import com.conspiracy.forum.exception.AuthenticationException;
import com.conspiracy.forum.exception.ValidationException;
import com.conspiracy.forum.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_ShouldCreateUser_WhenValidInput() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("testuser", response.getUsername());
        assertNotNull(response.getUserId());
        assertTrue(userRepository.existsByUsername("testuser"));
    }

    @Test
    void register_ShouldFail_WhenUsernameTooShort() {
        RegisterRequest request = RegisterRequest.builder()
                .username("ab")
                .email("test@example.com")
                .password("password123")
                .build();

        assertThrows(ValidationException.class, () -> authService.register(request));
    }

    @Test
    void register_ShouldFail_WhenUsernameExists() {
        RegisterRequest request1 = RegisterRequest.builder()
                .username("testuser")
                .email("test1@example.com")
                .password("password123")
                .build();
        authService.register(request1);

        RegisterRequest request2 = RegisterRequest.builder()
                .username("testuser")
                .email("test2@example.com")
                .password("password123")
                .build();

        assertThrows(ValidationException.class, () -> authService.register(request2));
    }

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() {
        // First register
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        authService.register(registerRequest);

        // Then login
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void login_ShouldFail_WhenInvalidPassword() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        authService.register(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));
    }

    @Test
    void register_WithSecretCode_ShouldSucceed_WhenCodeIsValid() {
        RegisterRequest request = RegisterRequest.builder()
                .username("secretuser")
                .email("secret@example.com")
                .password("password123")
                .secretCode("TESTCODE")
                .build();

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("secretuser", response.getUsername());
    }

    @Test
    void register_WithSecretCode_ShouldFail_WhenCodeIsInvalid() {
        RegisterRequest request = RegisterRequest.builder()
                .username("secretuser")
                .email("secret@example.com")
                .password("password123")
                .secretCode("WRONGCODE")
                .build();

        assertThrows(AuthenticationException.class, () -> authService.register(request));
    }
}
