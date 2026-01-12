package com.conspiracy.forum.resolver;

import com.conspiracy.forum.config.TestMailConfig;
import com.conspiracy.forum.dto.RegisterRequest;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.repository.CommentRepository;
import com.conspiracy.forum.repository.TheoryRepository;
import com.conspiracy.forum.repository.UserRepository;
import com.conspiracy.forum.repository.VoteRepository;
import com.conspiracy.forum.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureHttpGraphQlTester
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class AuthResolverTest {

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TheoryRepository theoryRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        commentRepository.deleteAll();
        theoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_ShouldReturnAuthResponse_WhenValidInput() {
        String mutation = """
            mutation {
                register(input: {
                    username: "newuser"
                    email: "newuser@example.com"
                    password: "password123"
                }) {
                    token
                    username
                    userId
                    message
                }
            }
            """;

        graphQlTester.document(mutation)
                .execute()
                .path("register.username").entity(String.class).isEqualTo("newuser")
                .path("register.token").entity(String.class).satisfies(token -> {
                    assertNotNull(token);
                    assertFalse(token.isEmpty());
                })
                .path("register.userId").entity(String.class).satisfies(userId -> {
                    assertNotNull(userId);
                })
                .path("register.message").entity(String.class).satisfies(message -> {
                    assertNotNull(message);
                });
    }

    @Test
    void register_ShouldReturnError_WhenUsernameExists() {
        // First create a user
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .email("existing@example.com")
                .password("password123")
                .build();
        authService.register(request);

        String mutation = """
            mutation {
                register(input: {
                    username: "existinguser"
                    email: "new@example.com"
                    password: "password123"
                }) {
                    token
                    username
                }
            }
            """;

        graphQlTester.document(mutation)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertFalse(errors.isEmpty());
                    assertTrue(errors.get(0).getMessage().contains("Username already exists"));
                });
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenValidCredentials() {
        // First register a user
        RegisterRequest request = RegisterRequest.builder()
                .username("loginuser")
                .email("login@example.com")
                .password("password123")
                .build();
        authService.register(request);

        String mutation = """
            mutation {
                login(input: {
                    username: "loginuser"
                    password: "password123"
                }) {
                    token
                    username
                    userId
                    message
                }
            }
            """;

        graphQlTester.document(mutation)
                .execute()
                .path("login.username").entity(String.class).isEqualTo("loginuser")
                .path("login.token").entity(String.class).satisfies(token -> {
                    assertNotNull(token);
                    assertFalse(token.isEmpty());
                });
    }

    @Test
    void login_ShouldReturnError_WhenInvalidPassword() {
        // First register a user
        RegisterRequest request = RegisterRequest.builder()
                .username("loginuser2")
                .email("login2@example.com")
                .password("password123")
                .build();
        authService.register(request);

        String mutation = """
            mutation {
                login(input: {
                    username: "loginuser2"
                    password: "wrongpassword"
                }) {
                    token
                    username
                }
            }
            """;

        graphQlTester.document(mutation)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertFalse(errors.isEmpty());
                });
    }

    @Test
    void register_WithValidSecretCode_ShouldSucceed() {
        String mutation = """
            mutation {
                register(input: {
                    username: "secretuser"
                    email: "secret@example.com"
                    password: "password123"
                    secretCode: "TESTCODE"
                }) {
                    token
                    username
                }
            }
            """;

        graphQlTester.document(mutation)
                .execute()
                .path("register.username").entity(String.class).isEqualTo("secretuser");
    }

    @Test
    void register_WithInvalidSecretCode_ShouldFail() {
        String mutation = """
            mutation {
                register(input: {
                    username: "secretuser2"
                    email: "secret2@example.com"
                    password: "password123"
                    secretCode: "WRONGCODE"
                }) {
                    token
                    username
                }
            }
            """;

        graphQlTester.document(mutation)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertFalse(errors.isEmpty());
                    assertTrue(errors.get(0).getMessage().contains("Invalid secret code"));
                });
    }
}
