package com.conspiracy.forum.service;

import com.conspiracy.forum.dto.AuthResponse;
import com.conspiracy.forum.dto.ChangePasswordRequest;
import com.conspiracy.forum.dto.ForgotPasswordRequest;
import com.conspiracy.forum.dto.LoginRequest;
import com.conspiracy.forum.dto.RegisterRequest;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.exception.AuthenticationException;
import com.conspiracy.forum.exception.ResourceNotFoundException;
import com.conspiracy.forum.exception.ValidationException;
import com.conspiracy.forum.repository.UserRepository;
import com.conspiracy.forum.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int TEMP_PASSWORD_LENGTH = 12;

    @Value("${forum.secret-code:TINFOIL2024}")
    private String validSecretCode;

    public AuthResponse register(RegisterRequest request) {
        if (request.getSecretCode() != null && !request.getSecretCode().isEmpty()) {
            if (!validSecretCode.equals(request.getSecretCode())) {
                throw new AuthenticationException("Invalid secret code. The truth remains hidden.");
            }
        }

        if (request.getUsername() == null || request.getUsername().length() < 3) {
            throw new ValidationException("Username must be at least 3 characters long");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

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

        String message = user.isMustChangePassword() 
                ? "Password reset required. Please change your password."
                : "The veil has been lifted. Welcome back.";

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userId(user.getId())
                .mustChangePassword(user.isMustChangePassword())
                .message(message)
                .build();
    }

    @Transactional
    public boolean changePassword(String username, ChangePasswordRequest request) {
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new ValidationException("New password must be at least 6 characters long");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        return true;
    }

    @Transactional
    public boolean forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email address"));

        // Generate temporary password
        String temporaryPassword = generateTemporaryPassword();

        // Update user with new password and set flag
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);

        // Send email with temporary password
        emailService.sendTemporaryPassword(request.getEmail(), temporaryPassword);

        return true;
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }
}
