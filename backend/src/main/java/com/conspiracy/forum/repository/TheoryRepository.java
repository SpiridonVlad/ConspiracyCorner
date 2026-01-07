package com.conspiracy.forum.repository;

import com.conspiracy.forum.entity.Theory;
import com.conspiracy.forum.enums.TheoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheoryRepository extends JpaRepository<Theory, Long> {
    
    Page<Theory> findByAuthorId(Long authorId, Pageable pageable);
    
    Page<Theory> findByStatus(TheoryStatus status, Pageable pageable);
    
    @Query("SELECT t FROM Theory t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(t.title) LIKE CONCAT('%', LOWER(:keyword), '%') OR " +
           "LOWER(t.content) LIKE CONCAT('%', LOWER(:keyword), '%'))")
    Page<Theory> findByFilters(@Param("status") TheoryStatus status, 
                               @Param("keyword") String keyword, 
                               Pageable pageable);
    
    @Query("SELECT t FROM Theory t WHERE t.commentCount >= :minComments ORDER BY t.commentCount DESC")
    Page<Theory> findHotTheories(@Param("minComments") int minComments, Pageable pageable);
    
    @Query("SELECT t FROM Theory t ORDER BY t.commentCount DESC")
    Page<Theory> findAllOrderByCommentCountDesc(Pageable pageable);
    
    List<Theory> findByAuthorIdOrderByPostedAtDesc(Long authorId);
}
