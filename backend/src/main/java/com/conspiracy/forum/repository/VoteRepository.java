package com.conspiracy.forum.repository;

import com.conspiracy.forum.entity.Comment;
import com.conspiracy.forum.entity.Theory;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserAndTheory(User user, Theory theory);
    Optional<Vote> findByUserAndComment(User user, Comment comment);
}
