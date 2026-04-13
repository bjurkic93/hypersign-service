package com.reddiax.rdxvideo.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for sending push notifications to devices via FCM.
 * Supports both TV devices (content refresh) and mobile devices (rewards/vouchers).
 */
public interface PushNotificationService {

    // ==================== TV Device Notifications ====================

    /**
     * Send content refresh notification to a specific device.
     * 
     * @param fcmToken the FCM token of the device
     */
    void sendContentRefresh(String fcmToken);

    /**
     * Send content refresh notification to all devices of an organization.
     * 
     * @param organizationId the organization ID
     */
    void sendContentRefreshToOrganization(Long organizationId);

    /**
     * Send restart notification to a specific device.
     * 
     * @param fcmToken the FCM token of the device
     */
    void sendRestartCommand(String fcmToken);

    /**
     * Send custom notification to a device.
     * 
     * @param fcmToken the FCM token of the device
     * @param action the action command (e.g., "REFRESH_CONTENT", "RESTART_APP")
     * @param data additional data map
     */
    void sendNotification(String fcmToken, String action, java.util.Map<String, String> data);

    /**
     * Send notification to multiple devices.
     * 
     * @param fcmTokens list of FCM tokens
     * @param action the action command
     * @param data additional data map
     */
    void sendNotificationToMultiple(List<String> fcmTokens, String action, java.util.Map<String, String> data);

    // ==================== Mobile User Notifications ====================

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
    void sendUserNotification(Long userId, String title, String body);

    /**
     * Send a generic notification to a specific device.
     *
     * @param fcmToken FCM token of the device
     * @param title notification title
     * @param body notification body
     */
    void sendNotificationToDevice(String fcmToken, String title, String body);
}
