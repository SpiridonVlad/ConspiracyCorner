package com.conspiracy.forum.resolver;

import com.conspiracy.forum.config.TestMailConfig;
import com.conspiracy.forum.dto.AuthResponse;
import com.conspiracy.forum.dto.CommentInput;
import com.conspiracy.forum.dto.RegisterRequest;
import com.conspiracy.forum.dto.TheoryInput;
import com.conspiracy.forum.entity.Comment;
import com.conspiracy.forum.entity.Theory;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.repository.CommentRepository;
import com.conspiracy.forum.repository.TheoryRepository;
import com.conspiracy.forum.repository.UserRepository;
import com.conspiracy.forum.repository.VoteRepository;
import com.conspiracy.forum.service.AuthService;
import com.conspiracy.forum.service.CommentService;
import com.conspiracy.forum.service.TheoryService;
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
class VoteResolverTest {

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

    @Autowired
    private CommentService commentService;

    private String authToken;
    private User testUser;
    private User authorUser;
    private Theory testTheory;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        commentRepository.deleteAll();
        theoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create voter user
        RegisterRequest voterRequest = RegisterRequest.builder()
                .username("voteuser")
                .email("vote@example.com")
                .password("password123")
                .build();
        AuthResponse authResponse = authService.register(voterRequest);
        authToken = authResponse.getToken();
        testUser = userRepository.findByUsername("voteuser").orElseThrow();

        // Create author user
        RegisterRequest authorRequest = RegisterRequest.builder()
                .username("authoruser")
                .email("author@example.com")
                .password("password123")
                .build();
        authService.register(authorRequest);
        authorUser = userRepository.findByUsername("authoruser").orElseThrow();

        // Create a theory by author
        TheoryInput theoryInput = TheoryInput.builder()
                .title("Test Theory for Voting")
                .content("This is a test theory for vote testing.")
                .build();
        testTheory = theoryService.createTheory(theoryInput, authorUser.getUsername());
    }

    private HttpGraphQlTester authenticatedTester() {
        return graphQlTester.mutate()
                .header("Authorization", "Bearer " + authToken)
                .build();
    }

    @Test
    void voteTheory_ShouldUpvote_WhenValueIsOne() {
        String mutation = """
            mutation($id: ID!) {
                voteTheory(id: $id, value: 1) {
                    id
                    title
                    score
                }
            }
            """;

        authenticatedTester().document(mutation)
                .variable("id", testTheory.getId())
                .execute()
                .path("voteTheory.score").entity(Integer.class).isEqualTo(1);
    }

    @Test
    void voteTheory_ShouldDownvote_WhenValueIsMinusOne() {
        String mutation = """
            mutation($id: ID!) {
                voteTheory(id: $id, value: -1) {
                    id
                    title
                    score
                }
            }
            """;

        authenticatedTester().document(mutation)
                .variable("id", testTheory.getId())
                .execute()
                .path("voteTheory.score").entity(Integer.class).isEqualTo(-1);
    }

    @Test
    void voteTheory_ShouldRemoveVote_WhenSameValueTwice() {
        String mutation = """
            mutation($id: ID!) {
                voteTheory(id: $id, value: 1) {
                    id
                    score
                }
            }
            """;

        // First vote
        authenticatedTester().document(mutation)
                .variable("id", testTheory.getId())
                .execute()
                .path("voteTheory.score").entity(Integer.class).isEqualTo(1);

        // Second vote (same value) should remove vote
        authenticatedTester().document(mutation)
                .variable("id", testTheory.getId())
                .execute()
                .path("voteTheory.score").entity(Integer.class).isEqualTo(0);
    }

    @Test
    void voteTheory_ShouldFail_WhenNotAuthenticated() {
        String mutation = """
            mutation($id: ID!) {
                voteTheory(id: $id, value: 1) {
                    id
                    score
                }
            }
            """;

        graphQlTester.document(mutation)
                .variable("id", testTheory.getId())
                .execute()
                .errors()
                .satisfy(errors -> assertFalse(errors.isEmpty()));
    }

    @Test
    void voteComment_ShouldUpvote_WhenValueIsOne() {
        // Create a comment
        CommentInput commentInput = CommentInput.builder()
                .content("Comment to vote on with enough content.")
                .theoryId(testTheory.getId())
                .build();
        Comment comment = commentService.createComment(commentInput, authorUser.getUsername());

        String mutation = """
            mutation($id: ID!) {
                voteComment(id: $id, value: 1) {
                    id
                    content
                    score
                }
            }
            """;

        authenticatedTester().document(mutation)
                .variable("id", comment.getId())
                .execute()
                .path("voteComment.score").entity(Integer.class).isEqualTo(1);
    }

    @Test
    void voteComment_ShouldDownvote_WhenValueIsMinusOne() {
        // Create a comment
        CommentInput commentInput = CommentInput.builder()
                .content("Comment to vote on with enough content.")
                .theoryId(testTheory.getId())
                .build();
        Comment comment = commentService.createComment(commentInput, authorUser.getUsername());

        String mutation = """
            mutation($id: ID!) {
                voteComment(id: $id, value: -1) {
                    id
                    content
                    score
                }
            }
            """;

        authenticatedTester().document(mutation)
                .variable("id", comment.getId())
                .execute()
                .path("voteComment.score").entity(Integer.class).isEqualTo(-1);
    }

    @Test
    void voteComment_ShouldRemoveVote_WhenSameValueTwice() {
        // Create a comment
        CommentInput commentInput = CommentInput.builder()
                .content("Comment to vote on with enough content.")
                .theoryId(testTheory.getId())
                .build();
        Comment comment = commentService.createComment(commentInput, authorUser.getUsername());

        String mutation = """
            mutation($id: ID!) {
                voteComment(id: $id, value: 1) {
                    id
                    score
                }
            }
            """;

        // First vote
        authenticatedTester().document(mutation)
                .variable("id", comment.getId())
                .execute()
                .path("voteComment.score").entity(Integer.class).isEqualTo(1);

        // Second vote (same value) should remove vote
        authenticatedTester().document(mutation)
                .variable("id", comment.getId())
                .execute()
                .path("voteComment.score").entity(Integer.class).isEqualTo(0);
    }

    @Test
    void voteComment_ShouldFail_WhenNotAuthenticated() {
        // Create a comment
        CommentInput commentInput = CommentInput.builder()
                .content("Comment to vote on with enough content.")
                .theoryId(testTheory.getId())
                .build();
        Comment comment = commentService.createComment(commentInput, authorUser.getUsername());

        String mutation = """
            mutation($id: ID!) {
                voteComment(id: $id, value: 1) {
                    id
                    score
                }
            }
            """;

        graphQlTester.document(mutation)
                .variable("id", comment.getId())
                .execute()
                .errors()
                .satisfy(errors -> assertFalse(errors.isEmpty()));
    }

    @Test
    void voteTheory_ShouldUpdateAuthorReputation() {
        int initialReputation = authorUser.getReputation();

        String mutation = """
            mutation($id: ID!) {
                voteTheory(id: $id, value: 1) {
                    id
                    score
                }
            }
            """;

        authenticatedTester().document(mutation)
                .variable("id", testTheory.getId())
                .execute();

        User updatedAuthor = userRepository.findById(authorUser.getId()).orElseThrow();
        assertEquals(initialReputation + 1, updatedAuthor.getReputation());
    }
}
