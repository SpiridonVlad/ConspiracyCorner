package com.conspiracy.forum;

import com.conspiracy.forum.config.TestMailConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class ForumApplicationTests {

    @Test
    void contextLoads() {
    }
}
