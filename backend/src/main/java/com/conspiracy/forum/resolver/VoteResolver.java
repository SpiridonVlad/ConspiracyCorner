package com.conspiracy.forum.resolver;

import com.conspiracy.forum.entity.Comment;
import com.conspiracy.forum.entity.Theory;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.service.UserService;
import com.conspiracy.forum.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class VoteResolver {

    private final VoteService voteService;
    private final UserService userService;

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Theory voteTheory(@Argument Long id, @Argument int value) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(authentication.getName());
        return voteService.voteTheory(currentUser, id, value);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Comment voteComment(@Argument Long id, @Argument int value) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(authentication.getName());
        return voteService.voteComment(currentUser, id, value);
    }
}
