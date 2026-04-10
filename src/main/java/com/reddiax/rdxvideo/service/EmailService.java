package com.reddiax.rdxvideo.service;

/**
 * Service for sending email notifications.
 */
public interface EmailService {

    /**
     * Send a simple text email.
     *
     * @param to recipient email address
     * @param subject email subject
     * @param body email body (plain text)
     */
    void sendEmail(String to, String subject, String body);

    /**
     * Send notification when an advertisement's budget is insufficient for new activations.
     *
     * @param advertisementId the advertisement ID
     * @param advertisementName the advertisement name
     * @param organizationName the organization name
     * @param organizationEmail the organization contact email (can be null)
     */
    void sendInsufficientBudgetNotification(Long advertisementId, String advertisementName, 
                                            String organizationName, String organizationEmail);

    /**
     * Send an HTML email.
     *
     * @param to recipient email address
     * @param subject email subject
     * @param htmlBody email body (HTML)
     */
    void sendHtmlEmail(String to, String subject, String htmlBody);
}
