package com.conspiracy.forum.resolver;

import com.conspiracy.forum.config.TestMailConfig;
import com.conspiracy.forum.dto.AuthResponse;
import com.conspiracy.forum.dto.RegisterRequest;
import com.conspiracy.forum.dto.TheoryInput;
import com.conspiracy.forum.entity.Theory;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.repository.CommentRepository;
import com.conspiracy.forum.repository.TheoryRepository;
import com.conspiracy.forum.repository.UserRepository;
import com.conspiracy.forum.repository.VoteRepository;
import com.conspiracy.forum.service.AuthService;
import com.conspiracy.forum.service.TheoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureHttpGraphQlTester
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class TheoryResolverTest {

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

    @Autowired
    private TheoryService theoryService;

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
                .username("theoryuser")
                .email("theory@example.com")
                .password("password123")
                .build();
        AuthResponse authResponse = authService.register(request);
        authToken = authResponse.getToken();
        testUser = userRepository.findByUsername("theoryuser").orElseThrow();
    }

    private HttpGraphQlTester authenticatedTester() {
        return graphQlTester.mutate()
                .header("Authorization", "Bearer " + authToken)
                .build();
    }

    @Test
    void theories_ShouldReturnEmptyList_WhenNoTheories() {
        String query = """
            query {
                theories {
                    id
                    title
                    content
                }
            }
            """;

        graphQlTester.document(query)
                .execute()
                .path("theories").entityList(Object.class).hasSize(0);
    }

    @Test
    void theories_ShouldReturnTheories_WhenExist() {
        // Create a theory
        TheoryInput input = TheoryInput.builder()
                .title("Test Theory Title")
                .content("This is test content with enough characters.")
                .build();
        theoryService.createTheory(input, testUser.getUsername());

        String query = """
            query {
                theories {
                    id
                    title
                    content
                    authorName
                }
            }
            """;

        graphQlTester.document(query)
                .execute()
                .path("theories").entityList(Object.class).hasSize(1)
                .path("theories[0].title").entity(String.class).isEqualTo("Test Theory Title")
                .path("theories[0].authorName").entity(String.class).isEqualTo("theoryuser");
    }

    @Test
    void theory_ShouldReturnTheory_WhenExists() {
        TheoryInput input = TheoryInput.builder()
                .title("Specific Theory")
                .content("This is specific content with enough characters.")
                .build();
        Theory created = theoryService.createTheory(input, testUser.getUsername());

        String query = """
            query($id: ID!) {
                theory(id: $id) {
                    id
                    title
                    content
                    status
                }
            }
            """;

        graphQlTester.document(query)
                .variable("id", created.getId())
                .execute()
                .path("theory.title").entity(String.class).isEqualTo("Specific Theory")
                .path("theory.status").entity(String.class).isEqualTo("UNVERIFIED");
    }

    @Test
    void theory_ShouldReturnNull_WhenNotFound() {
        String query = """
            query {
                theory(id: "99999") {
                    id
                    title
                }
            }
            """;

        graphQlTester.document(query)
                .execute()
                .errors()
                .satisfy(errors -> assertFalse(errors.isEmpty()));
    }

    @Test
    void createTheory_ShouldCreateTheory_WhenAuthenticated() {
        String mutation = """
            mutation {
                createTheory(input: {
                    title: "New Theory Title"
                    content: "This is new theory content with enough length."
                    status: UNVERIFIED
                }) {
                    id
                    title
                    content
                    status
                    authorName
                }
            }
            """;

        authenticatedTester().document(mutation)
                .execute()
                .path("createTheory.title").entity(String.class).isEqualTo("New Theory Title")
                .path("createTheory.status").entity(String.class).isEqualTo("UNVERIFIED")
                .path("createTheory.authorName").entity(String.class).isEqualTo("theoryuser");
    }

    @Test
    void createTheory_ShouldFail_WhenNotAuthenticated() {
        String mutation = """
            mutation {
                createTheory(input: {
                    title: "Unauthenticated Theory"
                    content: "This should fail because not authenticated."
                }) {
                    id
                    title
                }
            }
            """;

        graphQlTester.document(mutation)
                .execute()
                .errors()
                .satisfy(errors -> assertFalse(errors.isEmpty()));
    }

    @Test
    void updateTheory_ShouldUpdateTheory_WhenOwner() {
        TheoryInput input = TheoryInput.builder()
                .title("Original Title")
                .content("This is original content with enough chars.")
                .build();
        Theory created = theoryService.createTheory(input, testUser.getUsername());

        String mutation = """
            mutation($id: ID!) {
                updateTheory(id: $id, input: {
                    title: "Updated Title"
                    content: "This is updated content with enough chars."
                    status: CONFIRMED
                }) {
                    id
                    title
                    status
                }
            }
            """;

        authenticatedTester().document(mutation)
                .variable("id", created.getId())
                .execute()
                .path("updateTheory.title").entity(String.class).isEqualTo("Updated Title")
                .path("updateTheory.status").entity(String.class).isEqualTo("CONFIRMED");
    }

    @Test
    void deleteTheory_ShouldDeleteTheory_WhenOwner() {
        TheoryInput input = TheoryInput.builder()
                .title("Theory to Delete")
                .content("This theory will be deleted shortly.")
                .build();
        Theory created = theoryService.createTheory(input, testUser.getUsername());

        String mutation = """
            mutation($id: ID!) {
                deleteTheory(id: $id)
            }
            """;

        authenticatedTester().document(mutation)
                .variable("id", created.getId())
                .execute()
                .path("deleteTheory").entity(Boolean.class).isEqualTo(true);

        assertFalse(theoryRepository.existsById(created.getId()));
    }

    @Test
    void theoriesPaginated_ShouldReturnPaginatedResults() {
        // Create multiple theories
        for (int i = 0; i < 5; i++) {
            TheoryInput input = TheoryInput.builder()
                    .title("Theory " + i)
                    .content("Content for theory " + i + " with enough characters.")
                    .build();
            theoryService.createTheory(input, testUser.getUsername());
        }

        String query = """
            query {
                theoriesPaginated(page: { page: 1, size: 3 }) {
                    content {
                        id
                        title
                    }
                    totalElements
                    totalPages
                    currentPage
                    hasNext
                    hasPrevious
                }
            }
            """;

        graphQlTester.document(query)
                .execute()
                .path("theoriesPaginated.content").entityList(Object.class).hasSize(3)
                .path("theoriesPaginated.totalElements").entity(Integer.class).isEqualTo(5)
                .path("theoriesPaginated.totalPages").entity(Integer.class).isEqualTo(2)
                .path("theoriesPaginated.currentPage").entity(Integer.class).isEqualTo(1)
                .path("theoriesPaginated.hasNext").entity(Boolean.class).isEqualTo(true)
                .path("theoriesPaginated.hasPrevious").entity(Boolean.class).isEqualTo(false);
    }

    @Test
    void theories_WithFilter_ShouldFilterByKeyword() {
        TheoryInput input1 = TheoryInput.builder()
                .title("Aliens from Space")
                .content("Theory about aliens visiting Earth.")
                .build();
        theoryService.createTheory(input1, testUser.getUsername());

        TheoryInput input2 = TheoryInput.builder()
                .title("Moon Landing Hoax")
                .content("Theory about fake moon landing.")
                .build();
        theoryService.createTheory(input2, testUser.getUsername());

        String query = """
            query {
                theories(filter: { keyword: "aliens" }) {
                    id
                    title
                }
            }
            """;

        graphQlTester.document(query)
                .execute()
                .path("theories").entityList(Object.class).hasSize(1)
                .path("theories[0].title").entity(String.class).isEqualTo("Aliens from Space");
    }
}
