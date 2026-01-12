package com.conspiracy.forum.resolver;

import com.conspiracy.forum.dto.AuthResponse;
import com.conspiracy.forum.dto.ChangePasswordRequest;
import com.conspiracy.forum.dto.ForgotPasswordRequest;
import com.conspiracy.forum.dto.LoginRequest;
import com.conspiracy.forum.dto.RegisterRequest;
import com.conspiracy.forum.service.AuthService;
import com.conspiracy.forum.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AuthResolver {

    private final AuthService authService;

    @MutationMapping
    public AuthResponse register(@Argument RegisterRequest input) {
        return authService.register(input);
    }

    @MutationMapping
    public AuthResponse login(@Argument LoginRequest input) {
        return authService.login(input);
    }

    @MutationMapping
    public boolean changePassword(@Argument ChangePasswordRequest input) {
        String username = SecurityUtils.getAuthenticatedUsername();
        return authService.changePassword(username, input);
    }

    @MutationMapping
    public boolean forgotPassword(@Argument ForgotPasswordRequest input) {
        return authService.forgotPassword(input);
    }
}
