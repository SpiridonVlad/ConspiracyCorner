package com.conspiracy.forum.service;

import com.conspiracy.forum.dto.AuthResponse;
import com.conspiracy.forum.dto.LoginRequest;
import com.conspiracy.forum.dto.RegisterRequest;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.exception.AuthenticationException;
import com.conspiracy.forum.exception.ValidationException;
import com.conspiracy.forum.repository.UserRepository;
import com.conspiracy.forum.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${forum.secret-code:TINFOIL2024}")
    private String validSecretCode;

    public AuthResponse register(RegisterRequest request) {
        // Validate secret code if provided
        if (request.getSecretCode() != null && !request.getSecretCode().isEmpty()) {
            if (!validSecretCode.equals(request.getSecretCode())) {
                throw new AuthenticationException("Invalid secret code. The truth remains hidden.");
            }
        }

        // Validate username
        if (request.getUsername() == null || request.getUsername().length() < 3) {
            throw new ValidationException("Username must be at least 3 characters long");
        }

        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        // Validate password
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .secretCode(request.getSecretCode())
                .build();

        userRepository.save(user);
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userId(user.getId())
                .message("Welcome to the inner circle, truth seeker.")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Optional secret code validation for enhanced security
        if (request.getSecretCode() != null && !request.getSecretCode().isEmpty()) {
            if (!validSecretCode.equals(request.getSecretCode())) {
                throw new AuthenticationException("Invalid secret code. Access denied.");
            }
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new AuthenticationException("Invalid credentials. The shadows protect their secrets.");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userId(user.getId())
                .message("The veil has been lifted. Welcome back.")
                .build();
    }
}
