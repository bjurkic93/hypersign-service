package com.reddiax.rdxvideo.service.impl;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.reddiax.rdxvideo.repository.TvAuthSessionRepository;
import com.reddiax.rdxvideo.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of push notification service using Firebase Cloud Messaging.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {

    private final TvAuthSessionRepository tvAuthSessionRepository;

    @Override
    public void sendContentRefresh(String fcmToken) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("No FCM token provided, skipping push notification");
            return;
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("action", "REFRESH_CONTENT");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        sendNotification(fcmToken, "REFRESH_CONTENT", data);
    }

    @Override
    public void sendContentRefreshToOrganization(Long organizationId) {
        List<String> tokens = tvAuthSessionRepository.findFcmTokensByOrganizationId(organizationId);
        
        if (tokens.isEmpty()) {
            log.debug("No FCM tokens found for organization {}", organizationId);
            return;
        }
        
        log.info("Sending content refresh to {} devices of organization {}", tokens.size(), organizationId);
        
        Map<String, String> data = new HashMap<>();
        data.put("action", "REFRESH_CONTENT");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        sendNotificationToMultiple(tokens, "REFRESH_CONTENT", data);
    }

    @Override
    public void sendRestartCommand(String fcmToken) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("No FCM token provided, skipping push notification");
            return;
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("action", "RESTART_APP");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        sendNotification(fcmToken, "RESTART_APP", data);
    }

    @Override
    public void sendNotification(String fcmToken, String action, Map<String, String> data) {
        if (!isFirebaseInitialized()) {
            log.warn("Firebase not initialized, cannot send push notification");
            return;
        }
        
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("No FCM token provided, skipping push notification");
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .putAllData(data)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.debug("Push notification sent successfully: {}", response);
            
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED ||
                e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                log.warn("FCM token is invalid or unregistered: {}", fcmToken.substring(0, Math.min(20, fcmToken.length())));
                // TODO: Mark token as invalid in database
            } else {
                log.error("Failed to send push notification: {}", e.getMessage());
            }
        }
    }

    @Override
    public void sendNotificationToMultiple(List<String> fcmTokens, String action, Map<String, String> data) {
        if (!isFirebaseInitialized()) {
            log.warn("Firebase not initialized, cannot send push notifications");
            return;
        }
        
        List<String> validTokens = fcmTokens.stream()
                .filter(t -> t != null && !t.isBlank())
                .collect(Collectors.toList());
        
        if (validTokens.isEmpty()) {
            log.debug("No valid FCM tokens provided");
            return;
        }

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(validTokens)
                    .putAllData(data)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("Push notification sent to {} devices, {} successful, {} failed",
                    validTokens.size(), response.getSuccessCount(), response.getFailureCount());
            
            // Log failures
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        log.debug("Failed to send to token {}: {}", 
                                validTokens.get(i).substring(0, Math.min(20, validTokens.get(i).length())),
                                responses.get(i).getException().getMessage());
                    }
                }
            }
            
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send multicast push notification: {}", e.getMessage());
        }
    }

    private boolean isFirebaseInitialized() {
        return !FirebaseApp.getApps().isEmpty();
    }

    // ==================== Mobile User Notifications ====================

    @Override
    public void sendRewardEarnedNotification(Long userId, BigDecimal amount, String advertisementName) {
        // TODO: Implement user FCM token lookup
        log.info("Reward notification for user {}: {} RDX from {}", userId, amount, advertisementName);
    }

    @Override
    public void sendVoucherEarnedNotification(Long userId, String voucherCode, BigDecimal discountPercentage, String organizationName) {
        // TODO: Implement user FCM token lookup
        log.info("Voucher notification for user {}: {} ({}% off) from {}", userId, voucherCode, discountPercentage, organizationName);
    }

    @Override
    public void sendUserNotification(Long userId, String title, String body) {
        // TODO: Implement user FCM token lookup
        log.info("Notification for user {}: {} - {}", userId, title, body);
    }

    @Override
    public void sendNotificationToDevice(String fcmToken, String title, String body) {
        if (!isFirebaseInitialized()) {
            log.warn("Firebase not initialized, cannot send push notification");
            return;
        }

        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("No FCM token provided, skipping push notification");
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.debug("Push notification sent successfully: {}", response);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification: {}", e.getMessage());
        }
    }
}
