package com.conspiracy.forum.integration;

import com.conspiracy.forum.config.TestMailConfig;
import com.conspiracy.forum.dto.AuthResponse;
import com.conspiracy.forum.dto.CommentInput;
import com.conspiracy.forum.dto.RegisterRequest;
import com.conspiracy.forum.dto.TheoryInput;
import com.conspiracy.forum.entity.Comment;
import com.conspiracy.forum.entity.Theory;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.enums.TheoryStatus;
import com.conspiracy.forum.repository.CommentRepository;
import com.conspiracy.forum.repository.TheoryRepository;
import com.conspiracy.forum.repository.UserRepository;
import com.conspiracy.forum.repository.VoteRepository;
import com.conspiracy.forum.service.AuthService;
import com.conspiracy.forum.service.CommentService;
import com.conspiracy.forum.service.TheoryService;
import com.conspiracy.forum.service.VoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full integration tests that test the complete flow from GraphQL API through
 * all layers to the database and back.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureHttpGraphQlTester
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class FullForumIntegrationTest {

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

    @Autowired
    private VoteService voteService;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        commentRepository.deleteAll();
        theoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    private HttpGraphQlTester authenticatedTester(String token) {
        return graphQlTester.mutate()
                .header("Authorization", "Bearer " + token)
                .build();
    }

    /**
     * Tests the complete user registration, theory creation, comment posting,
     * and voting workflow.
     */
    @Test
    void completeForumWorkflow_ShouldWorkEndToEnd() {
        // Step 1: Register two users
        String registerMutation = """
            mutation($username: String!, $email: String!, $password: String!) {
                register(input: {
                    username: $username
                    email: $email
                    password: $password
                }) {
                    token
                    username
                    userId
                }
            }
            """;

        String user1Token = graphQlTester.document(registerMutation)
                .variable("username", "truthseeker1")
                .variable("email", "seeker1@example.com")
                .variable("password", "password123")
                .execute()
                .path("register.token").entity(String.class).get();

        String user2Token = graphQlTester.document(registerMutation)
                .variable("username", "truthseeker2")
                .variable("email", "seeker2@example.com")
                .variable("password", "password123")
                .execute()
                .path("register.token").entity(String.class).get();

        // Step 2: User 1 creates a theory
        String createTheoryMutation = """
            mutation {
                createTheory(input: {
                    title: "Area 51 Contains Alien Technology"
                    content: "I have evidence that Area 51 houses alien spacecraft and technology recovered from crash sites."
                    status: UNVERIFIED
                    evidenceUrls: ["https://example.com/evidence1"]
                }) {
                    id
                    title
                    status
                    score
                    commentCount
                }
            }
            """;

        String theoryId = authenticatedTester(user1Token).document(createTheoryMutation)
                .execute()
                .path("createTheory.id").entity(String.class).get();

        // Step 3: User 2 comments on the theory
        String createCommentMutation = """
            mutation($theoryId: ID!) {
                createComment(input: {
                    content: "Interesting theory! I have also seen similar reports from other sources."
                    theoryId: $theoryId
                }) {
                    id
                    content
                    authorName
                }
            }
            """;

        String commentId = authenticatedTester(user2Token).document(createCommentMutation)
                .variable("theoryId", theoryId)
                .execute()
                .path("createComment.id").entity(String.class).get();

        // Step 4: User 2 upvotes the theory
        String voteTheoryMutation = """
            mutation($id: ID!) {
                voteTheory(id: $id, value: 1) {
                    id
                    score
                }
            }
            """;

        authenticatedTester(user2Token).document(voteTheoryMutation)
                .variable("id", theoryId)
                .execute()
                .path("voteTheory.score").entity(Integer.class).isEqualTo(1);

        // Step 5: User 1 replies to the comment
        String replyMutation = """
            mutation($theoryId: ID!, $parentId: ID!) {
                createComment(input: {
                    content: "Thank you! I will be posting more evidence soon."
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

        authenticatedTester(user1Token).document(replyMutation)
                .variable("theoryId", theoryId)
                .variable("parentId", commentId)
                .execute()
                .path("createComment.parent.id").entity(String.class).isEqualTo(commentId);

        // Step 6: Verify the theory has correct counts and score
        String getTheoryQuery = """
            query($id: ID!) {
                theory(id: $id) {
                    id
                    title
                    score
                    commentCount
                    comments {
                        id
                        content
                    }
                    authorName
                }
            }
            """;

        graphQlTester.document(getTheoryQuery)
                .variable("id", theoryId)
                .execute()
                .path("theory.score").entity(Integer.class).isEqualTo(1)
                .path("theory.commentCount").entity(Integer.class).isEqualTo(2)
                .path("theory.authorName").entity(String.class).isEqualTo("truthseeker1");
    }

    /**
     * Tests anonymous posting functionality.
     */
    @Test
    void anonymousPosting_ShouldHideUserIdentity() {
        // Register a user
        RegisterRequest request = RegisterRequest.builder()
                .username("anonymoususer")
                .email("anon@example.com")
                .password("password123")
                .build();
        AuthResponse auth = authService.register(request);

        // Create an anonymous theory
        String createTheoryMutation = """
            mutation {
                createTheory(input: {
                    title: "Secret Government Program"
                    content: "I know about a secret program but cannot reveal my identity."
                    anonymousPost: true
                }) {
                    id
                    title
                    authorName
                    author {
                        id
                    }
                }
            }
            """;

        authenticatedTester(auth.getToken()).document(createTheoryMutation)
                .execute()
                .path("createTheory.authorName").entity(String.class).isEqualTo("Anonymous Truth Seeker")
                .path("createTheory.author").valueIsNull();
    }

    /**
     * Tests theory filtering and pagination.
     */
    @Test
    void theoryFilteringAndPagination_ShouldWorkCorrectly() {
        // Register a user
        RegisterRequest request = RegisterRequest.builder()
                .username("filteruser")
                .email("filter@example.com")
                .password("password123")
                .build();
        AuthResponse auth = authService.register(request);
        User user = userRepository.findByUsername("filteruser").orElseThrow();

        // Create multiple theories with different statuses
        TheoryInput unverified1 = TheoryInput.builder()
                .title("Unverified Theory One")
                .content("Content for unverified theory one.")
                .status(TheoryStatus.UNVERIFIED)
                .build();
        theoryService.createTheory(unverified1, user.getUsername());

        TheoryInput confirmed = TheoryInput.builder()
                .title("Confirmed Theory")
                .content("Content for confirmed theory here.")
                .status(TheoryStatus.CONFIRMED)
                .build();
        theoryService.createTheory(confirmed, user.getUsername());

        TheoryInput debunked = TheoryInput.builder()
                .title("Debunked Theory")
                .content("Content for debunked theory here.")
                .status(TheoryStatus.DEBUNKED)
                .build();
        theoryService.createTheory(debunked, user.getUsername());

        // Query with status filter
        String filterQuery = """
            query {
                theories(filter: { status: CONFIRMED }) {
                    id
                    title
                    status
                }
            }
            """;

        graphQlTester.document(filterQuery)
                .execute()
                .path("theories").entityList(Object.class).hasSize(1)
                .path("theories[0].title").entity(String.class).isEqualTo("Confirmed Theory");

        // Query with pagination
        String paginationQuery = """
            query {
                theoriesPaginated(page: { page: 1, size: 2 }) {
                    content {
                        id
                        title
                    }
                    totalElements
                    totalPages
                    hasNext
                }
            }
            """;

        graphQlTester.document(paginationQuery)
                .execute()
                .path("theoriesPaginated.totalElements").entity(Integer.class).isEqualTo(3)
                .path("theoriesPaginated.content").entityList(Object.class).hasSize(2)
                .path("theoriesPaginated.hasNext").entity(Boolean.class).isEqualTo(true);
    }

    /**
     * Tests that vote changes correctly update scores and reputation.
     */
    @Test
    void voteChanges_ShouldCorrectlyUpdateScoresAndReputation() {
        // Create two users
        RegisterRequest authorReq = RegisterRequest.builder()
                .username("author")
                .email("author@example.com")
                .password("password123")
                .build();
        authService.register(authorReq);
        User author = userRepository.findByUsername("author").orElseThrow();

        RegisterRequest voterReq = RegisterRequest.builder()
                .username("voter")
                .email("voter@example.com")
                .password("password123")
                .build();
        AuthResponse voterAuth = authService.register(voterReq);
        User voter = userRepository.findByUsername("voter").orElseThrow();

        // Create a theory
        TheoryInput input = TheoryInput.builder()
                .title("Theory for Voting Test")
                .content("This theory will be used to test voting mechanics.")
                .build();
        Theory theory = theoryService.createTheory(input, author.getUsername());

        // Initial state
        assertEquals(0, theory.getScore());
        assertEquals(0, author.getReputation());

        // Upvote
        voteService.voteTheory(voter, theory.getId(), 1);
        Theory afterUpvote = theoryRepository.findById(theory.getId()).orElseThrow();
        User authorAfterUpvote = userRepository.findById(author.getId()).orElseThrow();
        assertEquals(1, afterUpvote.getScore());
        assertEquals(1, authorAfterUpvote.getReputation());

        // Change to downvote
        voteService.voteTheory(voter, theory.getId(), -1);
        Theory afterDownvote = theoryRepository.findById(theory.getId()).orElseThrow();
        User authorAfterDownvote = userRepository.findById(author.getId()).orElseThrow();
        assertEquals(-1, afterDownvote.getScore());
        assertEquals(-1, authorAfterDownvote.getReputation());

        // Remove vote (click same button again)
        voteService.voteTheory(voter, theory.getId(), -1);
        Theory afterRemove = theoryRepository.findById(theory.getId()).orElseThrow();
        User authorAfterRemove = userRepository.findById(author.getId()).orElseThrow();
        assertEquals(0, afterRemove.getScore());
        assertEquals(0, authorAfterRemove.getReputation());
    }

    /**
     * Tests comment nesting and threading.
     */
    @Test
    void commentThreading_ShouldSupportNestedReplies() {
        // Register a user
        RegisterRequest request = RegisterRequest.builder()
                .username("threaduser")
                .email("thread@example.com")
                .password("password123")
                .build();
        AuthResponse auth = authService.register(request);
        User user = userRepository.findByUsername("threaduser").orElseThrow();

        // Create a theory
        TheoryInput theoryInput = TheoryInput.builder()
                .title("Theory for Threading Test")
                .content("Testing comment threading functionality.")
                .build();
        Theory theory = theoryService.createTheory(theoryInput, user.getUsername());

        // Create root comment
        CommentInput rootInput = CommentInput.builder()
                .content("This is the root comment.")
                .theoryId(theory.getId())
                .build();
        Comment rootComment = commentService.createComment(rootInput, user.getUsername());

        // Create first level reply
        CommentInput level1Input = CommentInput.builder()
                .content("This is a level 1 reply.")
                .theoryId(theory.getId())
                .parentId(rootComment.getId())
                .build();
        Comment level1Comment = commentService.createComment(level1Input, user.getUsername());

        // Create second level reply
        CommentInput level2Input = CommentInput.builder()
                .content("This is a level 2 reply.")
                .theoryId(theory.getId())
                .parentId(level1Comment.getId())
                .build();
        Comment level2Comment = commentService.createComment(level2Input, user.getUsername());

        // Verify structure via GraphQL
        String query = """
            query($theoryId: ID!) {
                rootCommentsByTheory(theoryId: $theoryId) {
                    id
                    content
                    replies {
                        id
                        content
                        replies {
                            id
                            content
                        }
                    }
                }
            }
            """;

        graphQlTester.document(query)
                .variable("theoryId", theory.getId())
                .execute()
                .path("rootCommentsByTheory").entityList(Object.class).hasSize(1)
                .path("rootCommentsByTheory[0].content").entity(String.class).isEqualTo("This is the root comment.")
                .path("rootCommentsByTheory[0].replies").entityList(Object.class).hasSize(1)
                .path("rootCommentsByTheory[0].replies[0].content").entity(String.class).isEqualTo("This is a level 1 reply.")
                .path("rootCommentsByTheory[0].replies[0].replies").entityList(Object.class).hasSize(1)
                .path("rootCommentsByTheory[0].replies[0].replies[0].content").entity(String.class).isEqualTo("This is a level 2 reply.");
    }

    /**
     * Tests that the current user query returns correct data.
     */
    @Test
    void meQuery_ShouldReturnCorrectUserData() {
        // Register a user
        RegisterRequest request = RegisterRequest.builder()
                .username("meuser")
                .email("me@example.com")
                .password("password123")
                .build();
        AuthResponse auth = authService.register(request);

        String query = """
            query {
                me {
                    id
                    username
                    email
                    anonymousMode
                    reputation
                }
            }
            """;

        authenticatedTester(auth.getToken()).document(query)
                .execute()
                .path("me.username").entity(String.class).isEqualTo("meuser")
                .path("me.email").entity(String.class).isEqualTo("me@example.com")
                .path("me.anonymousMode").entity(Boolean.class).isEqualTo(false)
                .path("me.reputation").entity(Integer.class).isEqualTo(0);
    }

    /**
     * Tests that hot theories query works correctly.
     */
    @Test
    void hotTheories_ShouldReturnTheoriesOrderedByComments() {
        // Register a user
        RegisterRequest request = RegisterRequest.builder()
                .username("hotuser")
                .email("hot@example.com")
                .password("password123")
                .build();
        authService.register(request);
        User user = userRepository.findByUsername("hotuser").orElseThrow();

        // Create theories with different comment counts
        TheoryInput coldInput = TheoryInput.builder()
                .title("Cold Theory")
                .content("This theory has no comments.")
                .build();
        theoryService.createTheory(coldInput, user.getUsername());

        TheoryInput hotInput = TheoryInput.builder()
                .title("Hot Theory")
                .content("This theory will have many comments.")
                .build();
        Theory hotTheory = theoryService.createTheory(hotInput, user.getUsername());

        // Add comments to hot theory
        for (int i = 0; i < 3; i++) {
            CommentInput commentInput = CommentInput.builder()
                    .content("Comment number " + i + " on hot theory.")
                    .theoryId(hotTheory.getId())
                    .build();
            commentService.createComment(commentInput, user.getUsername());
        }

        String query = """
            query {
                hotTheories {
                    id
                    title
                    commentCount
                }
            }
            """;

        graphQlTester.document(query)
                .execute()
                .path("hotTheories[0].title").entity(String.class).isEqualTo("Hot Theory")
                .path("hotTheories[0].commentCount").entity(Integer.class).isEqualTo(3);
    }
}
