package com.conspiracy.forum.service;

import com.conspiracy.forum.exception.EmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@truthforum.com}")
    private String fromEmail;

    public void sendTemporaryPassword(String toEmail, String temporaryPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Truth Forum - Your Temporary Password");
            message.setText(
                "Greetings, Truth Seeker!\n\n" +
                "Your temporary password has been generated:\n\n" +
                temporaryPassword + "\n\n" +
                "Please login with this password and change it immediately.\n\n" +
                "The truth is out there.\n" +
                "- Truth Forum Team"
            );
            mailSender.send(message);
            log.info("Temporary password email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new EmailException("Failed to send email. Please try again later.", e);
        }
    }
}
