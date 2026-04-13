package com.reddiax.rdxvideo.service;

import java.util.List;

/**
 * Service for sending push notifications to TV devices via FCM.
 */
public interface PushNotificationService {

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
}
