package com.conspiracy.forum.service;

import com.conspiracy.forum.config.TestMailConfig;
import com.conspiracy.forum.entity.User;
import com.conspiracy.forum.exception.ResourceNotFoundException;
import com.conspiracy.forum.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestMailConfig.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        User found = userService.getUserById(testUser.getId());

        assertNotNull(found);
        assertEquals(testUser.getId(), found.getId());
        assertEquals("testuser", found.getUsername());
    }

    @Test
    void getUserById_ShouldThrow_WhenNotFound() {
        assertThrows(ResourceNotFoundException.class, 
                () -> userService.getUserById(99999L));
    }

    @Test
    void getUserByUsername_ShouldReturnUser_WhenExists() {
        User found = userService.getUserByUsername("testuser");

        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
        assertEquals(testUser.getId(), found.getId());
    }

    @Test
    void getUserByUsername_ShouldThrow_WhenNotFound() {
        assertThrows(ResourceNotFoundException.class, 
                () -> userService.getUserByUsername("nonexistent"));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User secondUser = User.builder()
                .username("seconduser")
                .email("second@example.com")
                .password(passwordEncoder.encode("password123"))
                .build();
        userRepository.save(secondUser);

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void updateAnonymousSetting_ShouldSetToTrue() {
        assertFalse(testUser.isAnonymousMode());

        User updated = userService.updateAnonymousSetting("testuser", true);

        assertTrue(updated.isAnonymousMode());
    }

    @Test
    void updateAnonymousSetting_ShouldSetToFalse() {
        testUser.setAnonymousMode(true);
        userRepository.save(testUser);

        User updated = userService.updateAnonymousSetting("testuser", false);

        assertFalse(updated.isAnonymousMode());
    }

    @Test
    void updateAnonymousSetting_ShouldThrow_WhenUserNotFound() {
        assertThrows(ResourceNotFoundException.class, 
                () -> userService.updateAnonymousSetting("nonexistent", true));
    }
}
