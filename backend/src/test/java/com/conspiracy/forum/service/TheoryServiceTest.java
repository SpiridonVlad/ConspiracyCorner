package com.conspiracy.forum.service;

import com.conspiracy.forum.config.TestMailConfig;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestMailConfig.class)
class TheoryServiceTest {

    @Autowired
    private TheoryService theoryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TheoryRepository theoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        theoryRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("theorist")
                .email("theorist@example.com")
                .password(passwordEncoder.encode("password123"))
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    void createTheory_ShouldSucceed_WhenValidInput() {
        TheoryInput input = TheoryInput.builder()
                .title("Aliens Built the Pyramids")
                .content("This is a detailed theory about how aliens may have built the pyramids.")
                .status(TheoryStatus.UNVERIFIED)
                .evidenceUrls(List.of("https://example.com/evidence1"))
                .build();

        Theory theory = theoryService.createTheory(input, testUser.getUsername());

        assertNotNull(theory);
        assertNotNull(theory.getId());
        assertEquals("Aliens Built the Pyramids", theory.getTitle());
        assertEquals(TheoryStatus.UNVERIFIED, theory.getStatus());
        assertEquals(testUser.getId(), theory.getAuthor().getId());
    }

    @Test
    void createTheory_ShouldFail_WhenTitleTooShort() {
        TheoryInput input = TheoryInput.builder()
                .title("Hi")
                .content("This is a detailed theory about how aliens may have built the pyramids.")
                .build();

        assertThrows(ValidationException.class, 
                () -> theoryService.createTheory(input, testUser.getUsername()));
    }

    @Test
    void createTheory_ShouldFail_WhenContentTooShort() {
        TheoryInput input = TheoryInput.builder()
                .title("A Valid Title Here")
                .content("Too short")
                .build();

        assertThrows(ValidationException.class, 
                () -> theoryService.createTheory(input, testUser.getUsername()));
    }

    @Test
    void getTheoryById_ShouldReturnTheory_WhenExists() {
        TheoryInput input = TheoryInput.builder()
                .title("Secret Society Theory")
                .content("This is about secret societies controlling world events.")
                .build();
        Theory created = theoryService.createTheory(input, testUser.getUsername());

        Theory found = theoryService.getTheoryById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Secret Society Theory", found.getTitle());
    }

    @Test
    void getTheoryById_ShouldThrow_WhenNotFound() {
        assertThrows(ResourceNotFoundException.class, 
                () -> theoryService.getTheoryById(99999L));
    }

    @Test
    void updateTheory_ShouldSucceed_WhenOwner() {
        TheoryInput createInput = TheoryInput.builder()
                .title("Original Title Here")
                .content("Original content that is long enough for the test.")
                .build();
        Theory created = theoryService.createTheory(createInput, testUser.getUsername());

        TheoryInput updateInput = TheoryInput.builder()
                .title("Updated Title Here")
                .content("Updated content that is definitely long enough.")
                .status(TheoryStatus.CONFIRMED)
                .build();
        Theory updated = theoryService.updateTheory(created.getId(), updateInput, testUser.getUsername());

        assertEquals("Updated Title Here", updated.getTitle());
        assertEquals(TheoryStatus.CONFIRMED, updated.getStatus());
    }

    @Test
    void updateTheory_ShouldFail_WhenNotOwner() {
        TheoryInput createInput = TheoryInput.builder()
                .title("Original Title Here")
                .content("Original content that is long enough for the test.")
                .build();
        Theory created = theoryService.createTheory(createInput, testUser.getUsername());

        User otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .password(passwordEncoder.encode("password123"))
                .build();
        userRepository.save(otherUser);

        TheoryInput updateInput = TheoryInput.builder()
                .title("Hacked Title")
                .content("This should not be allowed at all.")
                .build();

        assertThrows(UnauthorizedException.class, 
                () -> theoryService.updateTheory(created.getId(), updateInput, otherUser.getUsername()));
    }

    @Test
    void deleteTheory_ShouldSucceed_WhenOwner() {
        TheoryInput input = TheoryInput.builder()
                .title("Theory to Delete")
                .content("This theory will be deleted soon.")
                .build();
        Theory created = theoryService.createTheory(input, testUser.getUsername());

        boolean result = theoryService.deleteTheory(created.getId(), testUser.getUsername());

        assertTrue(result);
        assertThrows(ResourceNotFoundException.class, 
                () -> theoryService.getTheoryById(created.getId()));
    }

    @Test
    void getTheories_WithFilter_ShouldFilterByStatus() {
        TheoryInput input1 = TheoryInput.builder()
                .title("Unverified Theory One")
                .content("This is an unverified theory content.")
                .status(TheoryStatus.UNVERIFIED)
                .build();
        theoryService.createTheory(input1, testUser.getUsername());

        TheoryInput input2 = TheoryInput.builder()
                .title("Confirmed Theory Two")
                .content("This is a confirmed theory content.")
                .status(TheoryStatus.CONFIRMED)
                .build();
        theoryService.createTheory(input2, testUser.getUsername());

        TheoryFilter filter = TheoryFilter.builder()
                .status(TheoryStatus.CONFIRMED)
                .build();
        PageInput pageInput = PageInput.builder().page(1).size(10).build();

        Page<Theory> result = theoryService.getTheories(filter, pageInput);

        assertEquals(1, result.getTotalElements());
        assertEquals(TheoryStatus.CONFIRMED, result.getContent().get(0).getStatus());
    }

    @Test
    void getTheories_WithFilter_ShouldFilterByKeyword() {
        TheoryInput input1 = TheoryInput.builder()
                .title("Aliens from Mars")
                .content("Theory about aliens coming from Mars to Earth.")
                .build();
        theoryService.createTheory(input1, testUser.getUsername());

        TheoryInput input2 = TheoryInput.builder()
                .title("Moon Landing Hoax")
                .content("Theory about the moon landing being faked.")
                .build();
        theoryService.createTheory(input2, testUser.getUsername());

        TheoryFilter filter = TheoryFilter.builder()
                .keyword("aliens")
                .build();
        PageInput pageInput = PageInput.builder().page(1).size(10).build();

        Page<Theory> result = theoryService.getTheories(filter, pageInput);

        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getTitle().contains("Aliens"));
    }
}
