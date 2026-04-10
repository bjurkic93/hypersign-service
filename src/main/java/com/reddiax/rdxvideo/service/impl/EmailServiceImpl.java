package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Email service implementation.
 * Currently logs notifications - actual email sending can be implemented with Spring Mail.
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("=== SENDING EMAIL ===");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Body:\n{}", body);
        log.info("=====================");
        
        // TODO: Implement actual email sending when Spring Mail is configured
        // Example implementation with Spring Mail:
        // SimpleMailMessage message = new SimpleMailMessage();
        // message.setTo(to);
        // message.setSubject(subject);
        // message.setText(body);
        // mailSender.send(message);
    }

    @Override
    public void sendInsufficientBudgetNotification(Long advertisementId, String advertisementName,
                                                   String organizationName, String organizationEmail) {
        log.warn("=== INSUFFICIENT BUDGET NOTIFICATION ===");
        log.warn("Advertisement ID: {}", advertisementId);
        log.warn("Advertisement Name: {}", advertisementName);
        log.warn("Organization: {}", organizationName);
        
        // For now, just log the notification
        // In production, you would send actual emails using Spring Mail
        log.warn("Organization contact email: {}", organizationEmail != null ? organizationEmail : "N/A");
        
        log.warn("=========================================");
        
        // TODO: Implement actual email sending when Spring Mail is configured
        // Example implementation with Spring Mail:
        // SimpleMailMessage message = new SimpleMailMessage();
        // message.setTo(organizationEmail);
        // message.setSubject("RdX Video: Advertisement Budget Alert - " + advertisementName);
        // message.setText("Your advertisement '" + advertisementName + "' has insufficient budget...");
        // mailSender.send(message);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        log.info("=== SENDING HTML EMAIL ===");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("HTML Body length: {} characters", htmlBody != null ? htmlBody.length() : 0);
        log.info("==========================");
        
        // TODO: Implement actual HTML email sending when Spring Mail is configured
        // Example implementation with Spring Mail:
        // MimeMessage message = mailSender.createMimeMessage();
        // MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        // helper.setTo(to);
        // helper.setSubject(subject);
        // helper.setText(htmlBody, true);
        // mailSender.send(message);
    }
}
