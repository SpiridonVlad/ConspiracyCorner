package com.conspiracy.forum.resolver;

import com.conspiracy.forum.config.TestMailConfig;
import com.conspiracy.forum.dto.AuthResponse;
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
class UserResolverTest {

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

    private String authToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        commentRepository.deleteAll();
        theoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create and authenticate a user
        RegisterRequest request = RegisterRequest.builder()
                .username("usertest")
                .email("user@example.com")
                .password("password123")
                .build();
        AuthResponse authResponse = authService.register(request);
        authToken = authResponse.getToken();
        testUser = userRepository.findByUsername("usertest").orElseThrow();
    }

    private HttpGraphQlTester authenticatedTester() {
        return graphQlTester.mutate()
                .header("Authorization", "Bearer " + authToken)
                .build();
    }

    @Test
    void user_ShouldReturnUser_WhenExists() {
        String query = """
            query($id: ID!) {
                user(id: $id) {
                    id
                    username
                    email
                    reputation
                }
            }
            """;

        graphQlTester.document(query)
                .variable("id", testUser.getId())
                .execute()
                .path("user.username").entity(String.class).isEqualTo("usertest")
                .path("user.email").entity(String.class).isEqualTo("user@example.com")
                .path("user.reputation").entity(Integer.class).isEqualTo(0);
    }

    @Test
    void user_ShouldReturnError_WhenNotFound() {
        String query = """
            query {
                user(id: "99999") {
                    id
                    username
                }
            }
            """;

        graphQlTester.document(query)
                .execute()
                .errors()
                .satisfy(errors -> assertFalse(errors.isEmpty()));
    }

    @Test
    void me_ShouldReturnCurrentUser_WhenAuthenticated() {
        String query = """
            query {
                me {
                    id
                    username
                    email
                    anonymousMode
                }
            }
            """;

        authenticatedTester().document(query)
                .execute()
                .path("me.username").entity(String.class).isEqualTo("usertest")
                .path("me.email").entity(String.class).isEqualTo("user@example.com")
                .path("me.anonymousMode").entity(Boolean.class).isEqualTo(false);
    }

    @Test
    void me_ShouldFail_WhenNotAuthenticated() {
        String query = """
            query {
                me {
                    id
                    username
                }
            }
            """;

        graphQlTester.document(query)
                .execute()
                .errors()
                .satisfy(errors -> assertFalse(errors.isEmpty()));
    }

    @Test
    void setAnonymousMode_ShouldUpdateMode_WhenAuthenticated() {
        String mutation = """
            mutation {
                setAnonymousMode(anonymous: true) {
                    id
                    username
                    anonymousMode
                }
            }
            """;

        authenticatedTester().document(mutation)
                .execute()
                .path("setAnonymousMode.anonymousMode").entity(Boolean.class).isEqualTo(true);

        // Verify in database
        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertTrue(updated.isAnonymousMode());
    }

    @Test
    void setAnonymousMode_ShouldDisableMode_WhenCalledWithFalse() {
        // First enable anonymous mode
        testUser.setAnonymousMode(true);
        userRepository.save(testUser);

        String mutation = """
            mutation {
                setAnonymousMode(anonymous: false) {
                    id
                    username
                    anonymousMode
                }
            }
            """;

        authenticatedTester().document(mutation)
                .execute()
                .path("setAnonymousMode.anonymousMode").entity(Boolean.class).isEqualTo(false);
    }

    @Test
    void setAnonymousMode_ShouldFail_WhenNotAuthenticated() {
        String mutation = """
            mutation {
                setAnonymousMode(anonymous: true) {
                    id
                    username
                }
            }
            """;

        graphQlTester.document(mutation)
                .execute()
                .errors()
                .satisfy(errors -> assertFalse(errors.isEmpty()));
    }
}
