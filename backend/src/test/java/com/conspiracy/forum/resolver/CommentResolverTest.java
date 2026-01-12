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
class CommentResolverTest {

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
    private Theory testTheory;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        commentRepository.deleteAll();
        theoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create and authenticate a user
        RegisterRequest request = RegisterRequest.builder()
                .username("commentuser")
                .email("comment@example.com")
                .password("password123")
                .build();
        AuthResponse authResponse = authService.register(request);
        authToken = authResponse.getToken();
        testUser = userRepository.findByUsername("commentuser").orElseThrow();

        // Create a theory
        TheoryInput theoryInput = TheoryInput.builder()
                .title("Test Theory for Comments")
                .content("This is a test theory for comment testing.")
                .build();
        testTheory = theoryService.createTheory(theoryInput, testUser.getUsername());
    }

    private HttpGraphQlTester authenticatedTester() {
        return graphQlTester.mutate()
                .header("Authorization", "Bearer " + authToken)
                .build();
    }

    @Test
    void commentsByTheory_ShouldReturnEmptyList_WhenNoComments() {
        String query = """
            query($theoryId: ID!) {
                commentsByTheory(theoryId: $theoryId) {
                    id
                    content
                }
            }
            """;

        graphQlTester.document(query)
                .variable("theoryId", testTheory.getId())
                .execute()
                .path("commentsByTheory").entityList(Object.class).hasSize(0);
    }

    @Test
    void commentsByTheory_ShouldReturnComments_WhenExist() {
        CommentInput input = CommentInput.builder()
                .content("This is a test comment with enough content.")
                .theoryId(testTheory.getId())
                .build();
        commentService.createComment(input, testUser.getUsername());

        String query = """
            query($theoryId: ID!) {
                commentsByTheory(theoryId: $theoryId) {
                    id
                    content
                    authorName
                }
            }
            """;

        graphQlTester.document(query)
                .variable("theoryId", testTheory.getId())
                .execute()
                .path("commentsByTheory").entityList(Object.class).hasSize(1)
                .path("commentsByTheory[0].authorName").entity(String.class).isEqualTo("commentuser");
    }

    @Test
    void comment_ShouldReturnComment_WhenExists() {
        CommentInput input = CommentInput.builder()
                .content("Specific comment content here.")
                .theoryId(testTheory.getId())
                .build();
        Comment created = commentService.createComment(input, testUser.getUsername());

        String query = """
            query($id: ID!) {
                comment(id: $id) {
                    id
                    content
                    authorName
                }
            }
            """;

        graphQlTester.document(query)
                .variable("id", created.getId())
                .execute()
                .path("comment.content").entity(String.class).isEqualTo("Specific comment content here.")
                .path("comment.authorName").entity(String.class).isEqualTo("commentuser");
    }

    @Test
    void createComment_ShouldCreateComment_WhenAuthenticated() {
        String mutation = """
            mutation($theoryId: ID!) {
                createComment(input: {
                    content: "New comment content with enough length."
                    theoryId: $theoryId
                }) {
                    id
                    content
                    authorName
                }
            }
            """;

        authenticatedTester().document(mutation)
                .variable("theoryId", testTheory.getId())
                .execute()
                .path("createComment.content").entity(String.class).isEqualTo("New comment content with enough length.")
                .path("createComment.authorName").entity(String.class).isEqualTo("commentuser");
    }

    @Test
    void createComment_ShouldFail_WhenNotAuthenticated() {
        String mutation = """
            mutation($theoryId: ID!) {
                createComment(input: {
                    content: "Unauthenticated comment should fail."
                    theoryId: $theoryId
                }) {
                    id
                    content
                }
            }
            """;

        graphQlTester.document(mutation)
                .variable("theoryId", testTheory.getId())
                .execute()
                .errors()
                .satisfy(errors -> assertFalse(errors.isEmpty()));
    }

    @Test
    void createComment_WithAnonymous_ShouldHideAuthor() {
        String mutation = """
            mutation($theoryId: ID!) {
                createComment(input: {
                    content: "Anonymous comment content here."
                    theoryId: $theoryId
                    anonymousPost: true
                }) {
                    id
                    content
                    authorName
                    isAnonymousPost
                }
            }
            """;

        authenticatedTester().document(mutation)
                .variable("theoryId", testTheory.getId())
                .execute()
                .path("createComment.authorName").entity(String.class).isEqualTo("Anonymous Truth Seeker");
    }

    @Test
    void updateComment_ShouldUpdateComment_WhenOwner() {
        CommentInput input = CommentInput.builder()
                .content("Original comment content here.")
                .theoryId(testTheory.getId())
                .build();
        Comment created = commentService.createComment(input, testUser.getUsername());

        String mutation = """
            mutation($id: ID!) {
                updateComment(id: $id, content: "Updated comment content here.") {
                    id
                    content
                }
            }
            """;

        authenticatedTester().document(mutation)
                .variable("id", created.getId())
                .execute()
                .path("updateComment.content").entity(String.class).isEqualTo("Updated comment content here.");
    }

    @Test
    void deleteComment_ShouldDeleteComment_WhenOwner() {
        CommentInput input = CommentInput.builder()
                .content("Comment to be deleted soon.")
                .theoryId(testTheory.getId())
                .build();
        Comment created = commentService.createComment(input, testUser.getUsername());

        String mutation = """
            mutation($id: ID!) {
                deleteComment(id: $id)
            }
            """;

        authenticatedTester().document(mutation)
                .variable("id", created.getId())
                .execute()
                .path("deleteComment").entity(Boolean.class).isEqualTo(true);

        assertFalse(commentRepository.existsById(created.getId()));
    }

    @Test
    void rootCommentsByTheory_ShouldReturnOnlyRootComments() {
        // Create a root comment
        CommentInput rootInput = CommentInput.builder()
                .content("This is a root comment content.")
                .theoryId(testTheory.getId())
                .build();
        Comment rootComment = commentService.createComment(rootInput, testUser.getUsername());

        // Create a reply to the root comment
        CommentInput replyInput = CommentInput.builder()
                .content("This is a reply comment content.")
                .theoryId(testTheory.getId())
                .parentId(rootComment.getId())
                .build();
        commentService.createComment(replyInput, testUser.getUsername());

        String query = """
            query($theoryId: ID!) {
                rootCommentsByTheory(theoryId: $theoryId) {
                    id
                    content
                }
            }
            """;

        graphQlTester.document(query)
                .variable("theoryId", testTheory.getId())
                .execute()
                .path("rootCommentsByTheory").entityList(Object.class).hasSize(1)
                .path("rootCommentsByTheory[0].content").entity(String.class).isEqualTo("This is a root comment content.");
    }

    @Test
    void createComment_AsReply_ShouldCreateNestedComment() {
        // Create a root comment
        CommentInput rootInput = CommentInput.builder()
                .content("This is a root comment content.")
                .theoryId(testTheory.getId())
                .build();
        Comment rootComment = commentService.createComment(rootInput, testUser.getUsername());

        String mutation = """
            mutation($theoryId: ID!, $parentId: ID!) {
                createComment(input: {
                    content: "This is a reply to the root comment."
                    theoryId: $theoryId
                    parentId: $parentId
                }) {
                    id
                    content
                    parent {
                        id
                    }
                }
            }
            """;

        authenticatedTester().document(mutation)
                .variable("theoryId", testTheory.getId())
                .variable("parentId", rootComment.getId())
                .execute()
                .path("createComment.content").entity(String.class).isEqualTo("This is a reply to the root comment.")
                .path("createComment.parent.id").entity(String.class).isEqualTo(rootComment.getId().toString());
    }
}
