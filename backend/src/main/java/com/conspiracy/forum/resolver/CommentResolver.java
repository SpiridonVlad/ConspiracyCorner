package com.conspiracy.forum.resolver;

import com.conspiracy.forum.dto.CommentInput;
import com.conspiracy.forum.dto.PageInput;
import com.conspiracy.forum.entity.Comment;
import com.conspiracy.forum.entity.Theory;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.service.CommentService;
import com.conspiracy.forum.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommentResolver {

    private final CommentService commentService;

    @QueryMapping
    public List<Comment> commentsByTheory(@Argument Long theoryId, @Argument PageInput page) {
        if (page != null) {
            return commentService.getCommentsByTheoryPaginated(theoryId, page).getContent();
        }
        return commentService.getCommentsByTheory(theoryId);
    }

    @QueryMapping
    public Comment comment(@Argument Long id) {
        return commentService.getCommentById(id);
    }

    @MutationMapping
    public Comment createComment(@Argument CommentInput input) {
        String username = SecurityUtils.getAuthenticatedUsername();
        return commentService.createComment(input, username);
    }

    @MutationMapping
    public Comment updateComment(@Argument Long id, @Argument String content) {
        String username = SecurityUtils.getAuthenticatedUsername();
        return commentService.updateComment(id, content, username);
    }

    @MutationMapping
    public boolean deleteComment(@Argument Long id) {
        String username = SecurityUtils.getAuthenticatedUsername();
        return commentService.deleteComment(id, username);
    }

    @SchemaMapping(typeName = "Comment", field = "author")
    public User getAuthor(Comment comment) {
        if (comment.isAnonymousPost()) {
            return null;
        }
        return comment.getAuthor();
    }

    @SchemaMapping(typeName = "Comment", field = "authorName")
    public String getAuthorName(Comment comment) {
        if (comment.isAnonymousPost()) {
            return "Anonymous Truth Seeker";
        }
        return comment.getAuthor().getUsername();
    }

    @SchemaMapping(typeName = "Comment", field = "theory")
    public Theory getTheory(Comment comment) {
        return comment.getTheory();
    }
}
