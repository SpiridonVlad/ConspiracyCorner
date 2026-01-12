package com.conspiracy.forum.service;

import com.conspiracy.forum.exception.EmailException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendTemporaryPassword_ShouldSendEmail_WhenMailSenderWorks() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@forum.com");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> 
                emailService.sendTemporaryPassword("user@example.com", "TempPass123"));

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendTemporaryPassword_ShouldThrowEmailException_WhenMailSenderFails() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@forum.com");
        doThrow(new MailException("SMTP Error") {}).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(EmailException.class, 
                () -> emailService.sendTemporaryPassword("user@example.com", "TempPass123"));
    }
}
