package com.cts.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setFrom("noreply@carbooking.com");
            message.setSubject("Password Reset Request");
            message.setText(
                    "Hi,\n\n" +
                            "Click the link below to reset your password:\n" +
                            resetLink + "\n\n" +
                            "This link expires in 15 minutes.\n\n" +
                            "Regards,\nCar Booking Team"
            );
            mailSender.send(message);
            System.out.println(" Email sent successfully to: " + toEmail);

        } catch (Exception e) {
            // Print exact error so we know what's failing
            System.out.println(" Email failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}