package com.conspiracy.forum.resolver;

import com.conspiracy.forum.dto.PageInput;
import com.conspiracy.forum.dto.TheoryFilter;
import com.conspiracy.forum.dto.TheoryInput;
import com.conspiracy.forum.entity.Comment;
import com.conspiracy.forum.entity.Theory;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.service.CommentService;
import com.conspiracy.forum.service.TheoryService;
import com.conspiracy.forum.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TheoryResolver {

    private final TheoryService theoryService;
    private final CommentService commentService;

    @QueryMapping
    public List<Theory> theories(@Argument TheoryFilter filter, @Argument PageInput page) {
        return theoryService.getTheories(filter, page).getContent();
    }

    @QueryMapping
    public Theory theory(@Argument Long id) {
        return theoryService.getTheoryById(id);
    }

    @QueryMapping
    public List<Theory> theoriesByUser(@Argument Long userId) {
        return theoryService.getTheoriesByUser(userId);
    }

    @QueryMapping
    public List<Theory> hotTheories(@Argument PageInput page) {
        return theoryService.getHotTheories(page).getContent();
    }

    @QueryMapping
    public TheoriesPage theoriesPaginated(@Argument TheoryFilter filter, @Argument PageInput page) {
        Page<Theory> result = theoryService.getTheories(filter, page);
        return new TheoriesPage(
                result.getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber() + 1,
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @MutationMapping
    public Theory createTheory(@Argument TheoryInput input) {
        String username = SecurityUtils.getAuthenticatedUsername();
        return theoryService.createTheory(input, username);
    }

    @MutationMapping
    public Theory updateTheory(@Argument Long id, @Argument TheoryInput input) {
        String username = SecurityUtils.getAuthenticatedUsername();
        return theoryService.updateTheory(id, input, username);
    }

    @MutationMapping
    public boolean deleteTheory(@Argument Long id) {
        String username = SecurityUtils.getAuthenticatedUsername();
        return theoryService.deleteTheory(id, username);
    }

    @SchemaMapping(typeName = "Theory", field = "comments")
    public List<Comment> getComments(Theory theory) {
        return commentService.getCommentsByTheory(theory.getId());
    }

    @SchemaMapping(typeName = "Theory", field = "author")
    public User getAuthor(Theory theory) {
        // Return null or anonymous placeholder if anonymous post
        if (theory.isAnonymousPost()) {
            return null;
        }
        return theory.getAuthor();
    }

    @SchemaMapping(typeName = "Theory", field = "authorName")
    public String getAuthorName(Theory theory) {
        if (theory.isAnonymousPost()) {
            return "Anonymous Truth Seeker";
        }
        return theory.getAuthor().getUsername();
    }

    public record TheoriesPage(
            List<Theory> content,
            long totalElements,
            int totalPages,
            int currentPage,
            boolean hasNext,
            boolean hasPrevious
    ) {}
}
