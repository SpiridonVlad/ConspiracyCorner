package com.conspiracy.forum.service;

import com.conspiracy.forum.dto.CommentInput;
import com.conspiracy.forum.dto.TheoryInput;
import com.conspiracy.forum.entity.Comment;
import com.conspiracy.forum.entity.Theory;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.exception.ResourceNotFoundException;
import com.conspiracy.forum.exception.UnauthorizedException;
import com.conspiracy.forum.exception.ValidationException;
import com.conspiracy.forum.repository.CommentRepository;
import com.conspiracy.forum.repository.TheoryRepository;
import com.conspiracy.forum.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private TheoryService theoryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TheoryRepository theoryRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Theory testTheory;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        theoryRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("commenter")
                .email("commenter@example.com")
                .password(passwordEncoder.encode("password123"))
                .build();
        testUser = userRepository.save(testUser);

        TheoryInput theoryInput = TheoryInput.builder()
                .title("Test Theory Title")
                .content("This is a test theory with enough content.")
                .build();
        testTheory = theoryService.createTheory(theoryInput, testUser.getUsername());
    }

    @Test
    void createComment_ShouldSucceed_WhenValidInput() {
        CommentInput input = CommentInput.builder()
                .content("This is a valid comment with enough characters.")
                .theoryId(testTheory.getId())
                .build();

        Comment comment = commentService.createComment(input, testUser.getUsername());

        assertNotNull(comment);
        assertNotNull(comment.getId());
        assertEquals(testTheory.getId(), comment.getTheory().getId());
        assertEquals(testUser.getId(), comment.getAuthor().getId());
    }

    @Test
    void createComment_ShouldFail_WhenContentTooShort() {
        CommentInput input = CommentInput.builder()
                .content("Short")
                .theoryId(testTheory.getId())
                .build();

        assertThrows(ValidationException.class, 
                () -> commentService.createComment(input, testUser.getUsername()));
    }

    @Test
    void createComment_ShouldIncrementTheoryCommentCount() {
        int initialCount = testTheory.getCommentCount();

        CommentInput input = CommentInput.builder()
                .content("A comment that is long enough to pass validation.")
                .theoryId(testTheory.getId())
                .build();
        commentService.createComment(input, testUser.getUsername());

        Theory updatedTheory = theoryService.getTheoryById(testTheory.getId());
        assertEquals(initialCount + 1, updatedTheory.getCommentCount());
    }

    @Test
    void getCommentsByTheory_ShouldReturnComments() {
        CommentInput input1 = CommentInput.builder()
                .content("First comment with enough content.")
                .theoryId(testTheory.getId())
                .build();
        commentService.createComment(input1, testUser.getUsername());

        CommentInput input2 = CommentInput.builder()
                .content("Second comment with enough content.")
                .theoryId(testTheory.getId())
                .build();
        commentService.createComment(input2, testUser.getUsername());

        List<Comment> comments = commentService.getCommentsByTheory(testTheory.getId());

        assertEquals(2, comments.size());
    }

    @Test
    void updateComment_ShouldSucceed_WhenOwner() {
        CommentInput input = CommentInput.builder()
                .content("Original comment content here.")
                .theoryId(testTheory.getId())
                .build();
        Comment created = commentService.createComment(input, testUser.getUsername());

        Comment updated = commentService.updateComment(
                created.getId(), 
                "Updated comment content here.", 
                testUser.getUsername()
        );

        assertEquals("Updated comment content here.", updated.getContent());
    }

    @Test
    void updateComment_ShouldFail_WhenNotOwner() {
        CommentInput input = CommentInput.builder()
                .content("Original comment content here.")
                .theoryId(testTheory.getId())
                .build();
        Comment created = commentService.createComment(input, testUser.getUsername());

        User otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .password(passwordEncoder.encode("password123"))
                .build();
        userRepository.save(otherUser);

        assertThrows(UnauthorizedException.class, 
                () -> commentService.updateComment(
                        created.getId(), 
                        "Hacked content", 
                        otherUser.getUsername()
                ));
    }

    @Test
    void deleteComment_ShouldSucceed_WhenOwner() {
        CommentInput input = CommentInput.builder()
                .content("Comment to be deleted soon.")
                .theoryId(testTheory.getId())
                .build();
        Comment created = commentService.createComment(input, testUser.getUsername());

        boolean result = commentService.deleteComment(created.getId(), testUser.getUsername());

        assertTrue(result);
        assertThrows(ResourceNotFoundException.class, 
                () -> commentService.getCommentById(created.getId()));
    }

    @Test
    void deleteComment_ShouldDecrementTheoryCommentCount() {
        CommentInput input = CommentInput.builder()
                .content("Comment that will be deleted.")
                .theoryId(testTheory.getId())
                .build();
        Comment created = commentService.createComment(input, testUser.getUsername());

        Theory theoryAfterAdd = theoryService.getTheoryById(testTheory.getId());
        int countAfterAdd = theoryAfterAdd.getCommentCount();

        commentService.deleteComment(created.getId(), testUser.getUsername());

        Theory theoryAfterDelete = theoryService.getTheoryById(testTheory.getId());
        assertEquals(countAfterAdd - 1, theoryAfterDelete.getCommentCount());
    }

    @Test
    void createComment_WithAnonymous_ShouldSetAnonymousFlag() {
        CommentInput input = CommentInput.builder()
                .content("Anonymous comment with enough content.")
                .theoryId(testTheory.getId())
                .anonymousPost(true)
                .build();

        Comment comment = commentService.createComment(input, testUser.getUsername());

        assertTrue(comment.isAnonymousPost());
    }
}
