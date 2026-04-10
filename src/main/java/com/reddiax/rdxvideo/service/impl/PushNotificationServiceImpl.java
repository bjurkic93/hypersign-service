package com.reddiax.rdxvideo.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.reddiax.rdxvideo.repository.DeviceRegistrationRepository;
import com.reddiax.rdxvideo.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * FCM Push Notification Service implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {

    private final DeviceRegistrationRepository deviceRegistrationRepository;

    @Override
    public void sendRewardEarnedNotification(Long userId, BigDecimal amount, String advertisementName) {
        String title = "Zaradili ste RDX!";
        String body = String.format("Upravo ste zaradili %s RDX za prikazivanje reklame \"%s\"", 
                amount.stripTrailingZeros().toPlainString(), advertisementName);
        
        sendNotificationWithData(userId, title, body, "REWARD_EARNED", amount.toPlainString());
    }

    @Override
    public void sendVoucherEarnedNotification(Long userId, String voucherCode, BigDecimal discountPercentage, String organizationName) {
        String title = "Novi voucher!";
        String body = String.format("Zaradili ste %s%% popust voucher od %s! Kod: %s", 
                discountPercentage.stripTrailingZeros().toPlainString(), organizationName, voucherCode);
        
        sendNotificationWithData(userId, title, body, "VOUCHER_EARNED", voucherCode);
    }

    @Override
    public void sendNotification(Long userId, String title, String body) {
        List<String> fcmTokens = deviceRegistrationRepository.findFcmTokensByUserId(userId);
        
        if (fcmTokens.isEmpty()) {
            log.debug("No FCM tokens found for user {}, skipping push notification", userId);
            return;
        }
        
        for (String fcmToken : fcmTokens) {
            sendNotificationToDevice(fcmToken, title, body);
        }
    }

    @Override
    public void sendNotificationToDevice(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            
            String response = FirebaseMessaging.getInstance().send(message);
            log.debug("Push notification sent successfully: {}", response);
            
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification to token {}: {}", fcmToken, e.getMessage());
            // TODO: Handle invalid tokens (e.g., remove from database)
            if (e.getMessagingErrorCode() != null) {
                switch (e.getMessagingErrorCode()) {
                    case UNREGISTERED, INVALID_ARGUMENT -> 
                        log.warn("FCM token is invalid or unregistered: {}", fcmToken);
                    default -> 
                        log.error("FCM error code: {}", e.getMessagingErrorCode());
                }
            }
        } catch (Exception e) {
            log.error("Unexpected error sending push notification: {}", e.getMessage());
        }
    }

    @Override
    public int sendNotificationToDevices(List<String> fcmTokens, String title, String body) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            log.debug("No FCM tokens provided, skipping batch notification");
            return 0;
        }
        
        int successCount = 0;
        int batchSize = 500; // FCM limit
        
        for (int i = 0; i < fcmTokens.size(); i += batchSize) {
            List<String> batch = fcmTokens.subList(i, Math.min(i + batchSize, fcmTokens.size()));
            
            for (String fcmToken : batch) {
                try {
                    Message message = Message.builder()
                            .setToken(fcmToken)
                            .setNotification(Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build())
                            .putData("type", "ANNOUNCEMENT")
                            .build();
                    
                    FirebaseMessaging.getInstance().send(message);
                    successCount++;
                    
                } catch (FirebaseMessagingException e) {
                    log.warn("Failed to send push to token: {}", e.getMessage());
                } catch (Exception e) {
                    log.error("Unexpected error in batch send: {}", e.getMessage());
                }
            }
            
            log.info("Batch push notification progress: {}/{} tokens processed", 
                    Math.min(i + batchSize, fcmTokens.size()), fcmTokens.size());
        }
        
        log.info("Batch push notification completed: {}/{} successful", successCount, fcmTokens.size());
        return successCount;
    }

    /**
     * Send notification with custom data payload.
     */
    private void sendNotificationWithData(Long userId, String title, String body, String type, String value) {
        List<String> fcmTokens = deviceRegistrationRepository.findFcmTokensByUserId(userId);
        
        if (fcmTokens.isEmpty()) {
            log.debug("No FCM tokens found for user {}, skipping push notification", userId);
            return;
        }
        
        for (String fcmToken : fcmTokens) {
            try {
                Message message = Message.builder()
                        .setToken(fcmToken)
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .putData("type", type)
                        .putData("value", value)
                        .build();
                
                String response = FirebaseMessaging.getInstance().send(message);
                log.debug("Push notification sent successfully to user {}: {}", userId, response);
                
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send push notification to user {}: {}", userId, e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error sending push notification to user {}: {}", userId, e.getMessage());
            }
        }
    }
}
