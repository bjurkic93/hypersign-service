package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.TvDeviceRegistrationDTO;
import com.reddiax.rdxvideo.model.dto.TvDeviceRegistrationRequestDTO;
import com.reddiax.rdxvideo.model.dto.TvDeviceRegistrationResponseDTO;

import java.util.List;

/**
 * Service for TV device registration and management.
 */
public interface TvDeviceRegistrationService {

    /**
     * Register a new TV device for an organization.
     * This stores the device's public key and returns a device token.
     */
    TvDeviceRegistrationResponseDTO registerDevice(TvDeviceRegistrationRequestDTO request);

    /**
     * Get device information by device token.
     * @param foreground when true, records that the app is currently in foreground (for "app active" in admin)
     */
    TvDeviceRegistrationDTO getDeviceByToken(String deviceToken, Boolean foreground);

    /**
     * Get all registered TV devices, optionally filtered by organization.
     * @param organizationId optional; when null, returns all active devices
     */
    List<TvDeviceRegistrationDTO> getAllDevices(Long organizationId);

    /**
     * Get all registered TV devices for an organization.
     */
    List<TvDeviceRegistrationDTO> getDevicesByOrganization(Long organizationId);

    /**
     * Validate device token and return the organization ID if valid.
     * Throws exception if device is not found, not active, or not trusted.
     */
    Long validateDeviceToken(String deviceToken);

    /**
     * Update device trust score.
     */
    void updateTrustScore(Long deviceId, int scoreChange, String reason);

    /**
     * Check if a device is trusted.
     */
    boolean isDeviceTrusted(String deviceToken);

    /**
     * Unregister (deactivate) a TV device.
     */
    void unregisterDevice(String deviceId, Long organizationId);

    /**
     * Update last seen timestamp for a device.
     */
    void updateLastSeen(String deviceToken);

    /**
     * Verify a signature from a TV device.
     */
    boolean verifyDeviceSignature(String deviceToken, String data, String signature);

    /**
     * Postavi zahtjev za pokretanje aplikacije na uređaju (s APIja). Uređaj pri sljedećem pollu vidi launchRequestedAt i može otvoriti app.
     */
    void requestLaunch(String deviceId, Long organizationId);

    /**
     * Ukloni zahtjev za pokretanje (nakon što uređaj otvori aplikaciju).
     */
    void clearLaunchRequest(String deviceToken);

    /**
     * Ažurira stanje HDMI/displaya na Android boxu – projicira li se slika (connected) ili ne.
     */
    void updateDisplayConnected(String deviceToken, boolean connected);
}
