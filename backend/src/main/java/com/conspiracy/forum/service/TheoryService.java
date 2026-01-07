package com.conspiracy.forum.service;

import com.conspiracy.forum.dto.PageInput;
import com.conspiracy.forum.dto.TheoryFilter;
import com.conspiracy.forum.dto.TheoryInput;
import com.conspiracy.forum.entity.Theory;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.enums.TheoryStatus;
import com.conspiracy.forum.exception.ResourceNotFoundException;
import com.conspiracy.forum.exception.UnauthorizedException;
import com.conspiracy.forum.exception.ValidationException;
import com.conspiracy.forum.repository.TheoryRepository;
import com.conspiracy.forum.repository.UserRepository;
import com.conspiracy.forum.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TheoryService {

    private final TheoryRepository theoryRepository;
    private final UserRepository userRepository;

    private static final int MIN_TITLE_LENGTH = 5;
    private static final int MIN_CONTENT_LENGTH = 20;
    private static final int HOT_THEORY_MIN_COMMENTS = 5;

    @Transactional(readOnly = true)
    public Page<Theory> getTheories(TheoryFilter filter, PageInput pageInput) {
        Pageable pageable = PaginationUtils.createPageable(pageInput);

        if (filter == null) {
            return theoryRepository.findAll(pageable);
        }

        // Hot theories filter
        if (Boolean.TRUE.equals(filter.getHotOnly())) {
            int minComments = filter.getMinCommentCount() != null ? 
                    filter.getMinCommentCount() : HOT_THEORY_MIN_COMMENTS;
            return theoryRepository.findHotTheories(minComments, pageable);
        }

        // Normalize empty keyword to null
        String keyword = filter.getKeyword();
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        // If no filters are provided, just return all
        if (filter.getStatus() == null && keyword == null) {
            return theoryRepository.findAll(pageable);
        }

        // Filter by status and/or keyword
        return theoryRepository.findByFilters(filter.getStatus(), keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Theory getTheoryById(Long id) {
        return theoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theory not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Theory> getTheoriesByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return theoryRepository.findByAuthorIdOrderByPostedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Page<Theory> getHotTheories(PageInput pageInput) {
        Pageable pageable = PaginationUtils.createPageable(pageInput);
        return theoryRepository.findAllOrderByCommentCountDesc(pageable);
    }

    @Transactional
    public Theory createTheory(TheoryInput input, String username) {
        validateTheoryInput(input);

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Theory theory = Theory.builder()
                .title(input.getTitle())
                .content(input.getContent())
                .status(input.getStatus() != null ? input.getStatus() : TheoryStatus.UNVERIFIED)
                .evidenceUrls(input.getEvidenceUrls() != null ? input.getEvidenceUrls() : new ArrayList<>())
                .isAnonymousPost(Boolean.TRUE.equals(input.getAnonymousPost()))
                .author(author)
                .build();

        return theoryRepository.save(theory);
    }

    @Transactional
    public Theory updateTheory(Long id, TheoryInput input, String username) {
        Theory theory = getTheoryById(id);
        
        // Check ownership
        if (!theory.getAuthor().getUsername().equals(username)) {
            throw new UnauthorizedException("You can only update your own theories");
        }

        if (input.getTitle() != null) {
            if (input.getTitle().length() < MIN_TITLE_LENGTH) {
                throw new ValidationException("Theory title must be at least " + MIN_TITLE_LENGTH + " characters");
            }
            theory.setTitle(input.getTitle());
        }

        if (input.getContent() != null) {
            if (input.getContent().length() < MIN_CONTENT_LENGTH) {
                throw new ValidationException("Theory content must be at least " + MIN_CONTENT_LENGTH + " characters");
            }
            theory.setContent(input.getContent());
        }

        if (input.getStatus() != null) {
            theory.setStatus(input.getStatus());
        }

        if (input.getEvidenceUrls() != null) {
            theory.setEvidenceUrls(input.getEvidenceUrls());
        }

        theory.setUpdatedAt(LocalDateTime.now());
        
        return theoryRepository.save(theory);
    }

    @Transactional
    public boolean deleteTheory(Long id, String username) {
        Theory theory = getTheoryById(id);
        
        // Check ownership
        if (!theory.getAuthor().getUsername().equals(username)) {
            throw new UnauthorizedException("You can only delete your own theories");
        }

        theoryRepository.delete(theory);
        return true;
    }

    private void validateTheoryInput(TheoryInput input) {
        if (input.getTitle() == null || input.getTitle().length() < MIN_TITLE_LENGTH) {
            throw new ValidationException("Theory title must be at least " + MIN_TITLE_LENGTH + " characters");
        }
        if (input.getContent() == null || input.getContent().length() < MIN_CONTENT_LENGTH) {
            throw new ValidationException("Theory content must be at least " + MIN_CONTENT_LENGTH + " characters");
        }
    }
}
