package com.conspiracy.forum.service;

import com.conspiracy.forum.config.TestMailConfig;
import com.conspiracy.forum.dto.TheoryInput;
import com.conspiracy.forum.entity.Comment;
import com.conspiracy.forum.entity.Theory;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.entity.Vote;
import com.conspiracy.forum.repository.CommentRepository;
import com.conspiracy.forum.repository.TheoryRepository;
import com.conspiracy.forum.repository.UserRepository;
import com.conspiracy.forum.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestMailConfig.class)
class VoteServiceTest {

    @Autowired
    private VoteService voteService;

    @Autowired
    private TheoryService theoryService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TheoryRepository theoryRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User otherUser;
    private Theory testTheory;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        commentRepository.deleteAll();
        theoryRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("voter")
                .email("voter@example.com")
                .password(passwordEncoder.encode("password123"))
                .build();
        testUser = userRepository.save(testUser);

        otherUser = User.builder()
                .username("author")
                .email("author@example.com")
                .password(passwordEncoder.encode("password123"))
                .build();
        otherUser = userRepository.save(otherUser);

        TheoryInput theoryInput = TheoryInput.builder()
                .title("Test Theory Title")
                .content("This is a test theory with enough content.")
                .build();
        testTheory = theoryService.createTheory(theoryInput, otherUser.getUsername());
    }

    @Test
    void voteTheory_ShouldUpvote_WhenValueIsOne() {
        Theory result = voteService.voteTheory(testUser, testTheory.getId(), 1);

        assertEquals(1, result.getScore());
    }

    @Test
    void voteTheory_ShouldDownvote_WhenValueIsMinusOne() {
        Theory result = voteService.voteTheory(testUser, testTheory.getId(), -1);

        assertEquals(-1, result.getScore());
    }

    @Test
    void voteTheory_ShouldRemoveVote_WhenVotingSameValueTwice() {
        voteService.voteTheory(testUser, testTheory.getId(), 1);
        Theory result = voteService.voteTheory(testUser, testTheory.getId(), 1);

        assertEquals(0, result.getScore());
    }

    @Test
    void voteTheory_ShouldChangeVote_WhenVotingDifferentValue() {
        voteService.voteTheory(testUser, testTheory.getId(), 1);
        Theory result = voteService.voteTheory(testUser, testTheory.getId(), -1);

        assertEquals(-1, result.getScore());
    }

    @Test
    void voteTheory_ShouldUpdateAuthorReputation() {
        int initialReputation = otherUser.getReputation();

        voteService.voteTheory(testUser, testTheory.getId(), 1);

        User updatedAuthor = userRepository.findById(otherUser.getId()).orElseThrow();
        assertEquals(initialReputation + 1, updatedAuthor.getReputation());
    }

    @Test
    void voteTheory_ShouldThrow_WhenInvalidValue() {
        assertThrows(IllegalArgumentException.class, 
                () -> voteService.voteTheory(testUser, testTheory.getId(), 0));
        assertThrows(IllegalArgumentException.class, 
                () -> voteService.voteTheory(testUser, testTheory.getId(), 2));
    }

    @Test
    void voteTheory_ShouldThrow_WhenTheoryNotFound() {
        assertThrows(RuntimeException.class, 
                () -> voteService.voteTheory(testUser, 99999L, 1));
    }

    @Test
    void voteComment_ShouldUpvote_WhenValueIsOne() {
        Comment comment = createTestComment();

        Comment result = voteService.voteComment(testUser, comment.getId(), 1);

        assertEquals(1, result.getScore());
    }

    @Test
    void voteComment_ShouldDownvote_WhenValueIsMinusOne() {
        Comment comment = createTestComment();

        Comment result = voteService.voteComment(testUser, comment.getId(), -1);

        assertEquals(-1, result.getScore());
    }

    @Test
    void voteComment_ShouldRemoveVote_WhenVotingSameValueTwice() {
        Comment comment = createTestComment();

        voteService.voteComment(testUser, comment.getId(), 1);
        Comment result = voteService.voteComment(testUser, comment.getId(), 1);

        assertEquals(0, result.getScore());
    }

    @Test
    void voteComment_ShouldChangeVote_WhenVotingDifferentValue() {
        Comment comment = createTestComment();

        voteService.voteComment(testUser, comment.getId(), 1);
        Comment result = voteService.voteComment(testUser, comment.getId(), -1);

        assertEquals(-1, result.getScore());
    }

    @Test
    void voteComment_ShouldUpdateAuthorReputation() {
        Comment comment = createTestComment();
        User commentAuthor = comment.getAuthor();
        int initialReputation = commentAuthor.getReputation();

        voteService.voteComment(testUser, comment.getId(), 1);

        User updatedAuthor = userRepository.findById(commentAuthor.getId()).orElseThrow();
        assertEquals(initialReputation + 1, updatedAuthor.getReputation());
    }

    @Test
    void voteComment_ShouldThrow_WhenInvalidValue() {
        Comment comment = createTestComment();

        assertThrows(IllegalArgumentException.class, 
                () -> voteService.voteComment(testUser, comment.getId(), 0));
        assertThrows(IllegalArgumentException.class, 
                () -> voteService.voteComment(testUser, comment.getId(), 2));
    }

    @Test
    void voteComment_ShouldThrow_WhenCommentNotFound() {
        assertThrows(RuntimeException.class, 
                () -> voteService.voteComment(testUser, 99999L, 1));
    }

    private Comment createTestComment() {
        Comment comment = Comment.builder()
                .content("Test comment with enough content.")
                .author(otherUser)
                .theory(testTheory)
                .build();
        return commentRepository.save(comment);
    }
}
