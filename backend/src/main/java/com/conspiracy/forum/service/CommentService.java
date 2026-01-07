package com.conspiracy.forum.service;

import com.conspiracy.forum.dto.CommentInput;
import com.conspiracy.forum.dto.PageInput;
import com.conspiracy.forum.entity.Comment;
import com.conspiracy.forum.entity.Theory;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.exception.ResourceNotFoundException;
import com.conspiracy.forum.exception.UnauthorizedException;
import com.conspiracy.forum.exception.ValidationException;
import com.conspiracy.forum.repository.CommentRepository;
import com.conspiracy.forum.repository.TheoryRepository;
import com.conspiracy.forum.repository.UserRepository;
import com.conspiracy.forum.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TheoryRepository theoryRepository;
    private final UserRepository userRepository;

    private static final int MIN_CONTENT_LENGTH = 10;

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByTheory(Long theoryId) {
        if (!theoryRepository.existsById(theoryId)) {
            throw new ResourceNotFoundException("Theory not found with id: " + theoryId);
        }
        return commentRepository.findByTheoryIdOrderByPostedAtDesc(theoryId);
    }

    @Transactional(readOnly = true)
    public Page<Comment> getCommentsByTheoryPaginated(Long theoryId, PageInput pageInput) {
        if (!theoryRepository.existsById(theoryId)) {
            throw new ResourceNotFoundException("Theory not found with id: " + theoryId);
        }
        Pageable pageable = PaginationUtils.createPageable(pageInput);
        return commentRepository.findByTheoryId(theoryId, pageable);
    }

    @Transactional(readOnly = true)
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
    }

    @Transactional
    public Comment createComment(CommentInput input, String username) {
        validateCommentInput(input);

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Theory theory = theoryRepository.findById(input.getTheoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Theory not found with id: " + input.getTheoryId()));

        Comment comment = Comment.builder()
                .content(input.getContent())
                .isAnonymousPost(Boolean.TRUE.equals(input.getAnonymousPost()))
                .author(author)
                .theory(theory)
                .build();

        Comment savedComment = commentRepository.save(comment);
        
        // Update theory comment count
        theory.incrementCommentCount();
        theoryRepository.save(theory);

        return savedComment;
    }

    @Transactional
    public Comment updateComment(Long id, String content, String username) {
        if (content == null || content.length() < MIN_CONTENT_LENGTH) {
            throw new ValidationException("Comment content must be at least " + MIN_CONTENT_LENGTH + " characters");
        }

        Comment comment = getCommentById(id);

        // Check ownership
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new UnauthorizedException("You can only update your own comments");
        }

        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    @Transactional
    public boolean deleteComment(Long id, String username) {
        Comment comment = getCommentById(id);

        // Check ownership
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new UnauthorizedException("You can only delete your own comments");
        }

        // Update theory comment count
        Theory theory = comment.getTheory();
        theory.decrementCommentCount();
        theoryRepository.save(theory);

        commentRepository.delete(comment);
        return true;
    }

    private void validateCommentInput(CommentInput input) {
        if (input.getContent() == null || input.getContent().length() < MIN_CONTENT_LENGTH) {
            throw new ValidationException("Comment content must be at least " + MIN_CONTENT_LENGTH + " characters");
        }
        if (input.getTheoryId() == null) {
            throw new ValidationException("Theory ID is required");
        }
    }
}
