package com.conspiracy.forum.service;

import com.conspiracy.forum.entity.Comment;
import com.conspiracy.forum.entity.Theory;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.entity.Vote;
import com.conspiracy.forum.repository.CommentRepository;
import com.conspiracy.forum.repository.TheoryRepository;
import com.conspiracy.forum.repository.UserRepository;
import com.conspiracy.forum.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final TheoryRepository theoryRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public Theory voteTheory(User user, Long theoryId, int value) {
        if (value != 1 && value != -1) {
            throw new IllegalArgumentException("Vote value must be 1 or -1");
        }

        Theory theory = theoryRepository.findById(theoryId)
                .orElseThrow(() -> new RuntimeException("Theory not found"));

        Optional<Vote> existingVote = voteRepository.findByUserAndTheory(user, theory);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getValue() == value) {
                // Toggle vote: remove if same value
                voteRepository.delete(vote);
                theory.setScore(theory.getScore() - value);
                updateUserReputation(theory.getAuthor(), -value); // Decrease rep if vote removed
            } else {
                // Change vote
                int scoreChange = value - vote.getValue(); // e.g., -1 to 1 is +2
                theory.setScore(theory.getScore() + scoreChange);
                vote.setValue(value);
                voteRepository.save(vote);
                // Update rep: remove old value effect, add new value effect
                updateUserReputation(theory.getAuthor(), scoreChange);
            }
        } else {
            // New vote
            Vote vote = Vote.builder()
                    .user(user)
                    .theory(theory)
                    .value(value)
                    .build();
            voteRepository.save(vote);
            theory.setScore(theory.getScore() + value);
            updateUserReputation(theory.getAuthor(), value);
        }

        return theoryRepository.save(theory);
    }

    @Transactional
    public Comment voteComment(User user, Long commentId, int value) {
        if (value != 1 && value != -1) {
            throw new IllegalArgumentException("Vote value must be 1 or -1");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Optional<Vote> existingVote = voteRepository.findByUserAndComment(user, comment);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getValue() == value) {
                // Toggle vote
                voteRepository.delete(vote);
                comment.setScore(comment.getScore() - value);
                // Comments affect rep less or not at all? Let's say comments affect rep by 1 too
                updateUserReputation(comment.getAuthor(), -value);
            } else {
                // Change vote
                int scoreChange = value - vote.getValue();
                comment.setScore(comment.getScore() + scoreChange);
                vote.setValue(value);
                voteRepository.save(vote);
                updateUserReputation(comment.getAuthor(), scoreChange);
            }
        } else {
            // New vote
            Vote vote = Vote.builder()
                    .user(user)
                    .comment(comment)
                    .value(value)
                    .build();
            voteRepository.save(vote);
            comment.setScore(comment.getScore() + value);
            updateUserReputation(comment.getAuthor(), value);
        }

        return commentRepository.save(comment);
    }

    private void updateUserReputation(User user, int change) {
        user.setReputation(user.getReputation() + change);
        userRepository.save(user);
    }
}
