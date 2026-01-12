package com.conspiracy.forum.repository;

import com.conspiracy.forum.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTheoryIdOrderByPostedAtDesc(Long theoryId);
    List<Comment> findByTheoryIdAndParentIsNullOrderByPostedAtDesc(Long theoryId);
    List<Comment> findByParentIdOrderByPostedAtAsc(Long parentId);
    Page<Comment> findByTheoryId(Long theoryId, Pageable pageable);
    Page<Comment> findByTheoryIdAndParentIsNull(Long theoryId, Pageable pageable);
    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);
    int countByTheoryId(Long theoryId);
}
