package com.conspiracy.forum.resolver;

import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.service.UserService;
import com.conspiracy.forum.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserResolver {

    private final UserService userService;

    @QueryMapping
    public User user(@Argument Long id) {
        return userService.getUserById(id);
    }

    @QueryMapping
    public User me() {
        String username = SecurityUtils.getAuthenticatedUsername();
        return userService.getUserByUsername(username);
    }

    @MutationMapping
    public User setAnonymousMode(@Argument boolean anonymous) {
        String username = SecurityUtils.getAuthenticatedUsername();
        return userService.updateAnonymousSetting(username, anonymous);
    }
}
