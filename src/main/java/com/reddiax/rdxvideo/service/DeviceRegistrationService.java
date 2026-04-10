package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.DeviceRegistrationDTO;
import com.reddiax.rdxvideo.model.dto.DeviceRegistrationRequestDTO;
import com.reddiax.rdxvideo.model.dto.DeviceRegistrationResponseDTO;

import java.util.List;

public interface DeviceRegistrationService {
    
    /**
     * Register a new device for the current user.
     * Generates a device token and stores the public key.
     */
    DeviceRegistrationResponseDTO registerDevice(DeviceRegistrationRequestDTO request);
    
    /**
     * Get device registration by device token.
     */
    DeviceRegistrationDTO getDeviceByToken(String deviceToken);
    
    /**
     * Get all devices registered for the current user.
     */
    List<DeviceRegistrationDTO> getMyDevices();
    
    /**
     * Update device trust score.
     */
    void updateTrustScore(Long deviceId, int scoreChange, String reason);
    
    /**
     * Check if a device is trusted.
     */
    boolean isDeviceTrusted(String deviceToken);
    
    /**
     * Remove device registration.
     */
    void unregisterDevice(String deviceId);
    
    /**
     * Update last seen timestamp for a device.
     */
    void updateLastSeen(String deviceToken);
    
    /**
     * Update FCM token for push notifications.
     * 
     * @param deviceToken device token
     * @param fcmToken FCM token from Firebase
     */
    void updateFcmToken(String deviceToken, String fcmToken);
}
