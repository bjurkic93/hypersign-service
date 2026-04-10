package com.reddiax.rdxvideo.service;

import java.math.BigDecimal;

/**
 * Service for sending push notifications to mobile devices via FCM.
 */
public interface PushNotificationService {

    /**
     * Send a reward notification when user earns RDX coins.
     *
     * @param userId user ID
     * @param amount amount of RDX coins earned
     * @param advertisementName name of the advertisement
     */
    void sendRewardEarnedNotification(Long userId, BigDecimal amount, String advertisementName);

    /**
     * Send a voucher notification when user earns a discount voucher.
     *
     * @param userId user ID
     * @param voucherCode the voucher code
     * @param discountPercentage discount percentage
     * @param organizationName name of the organization
     */
    void sendVoucherEarnedNotification(Long userId, String voucherCode, BigDecimal discountPercentage, String organizationName);

    /**
     * Send a generic notification to a user.
     *
     * @param userId user ID
     * @param title notification title
     * @param body notification body
     */
    void sendNotification(Long userId, String title, String body);

    /**
     * Send a generic notification to a specific device.
     *
     * @param fcmToken FCM token of the device
     * @param title notification title
     * @param body notification body
     */
    void sendNotificationToDevice(String fcmToken, String title, String body);

    /**
     * Send a notification to multiple devices (batch).
     * Returns the number of successful deliveries.
     *
     * @param fcmTokens list of FCM tokens
     * @param title notification title
     * @param body notification body
     * @return number of successful deliveries
     */
    int sendNotificationToDevices(java.util.List<String> fcmTokens, String title, String body);
}
