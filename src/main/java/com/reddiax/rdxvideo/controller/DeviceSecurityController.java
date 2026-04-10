package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.*;
import com.reddiax.rdxvideo.service.ChallengeService;
import com.reddiax.rdxvideo.service.DeviceRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for device security operations.
 * Handles device registration, challenge-response protocol, and device management.
 */
@RestController
@RequestMapping("/api/v1/mobile/devices")
@RequiredArgsConstructor
@Slf4j
public class DeviceSecurityController {
    
    private final DeviceRegistrationService deviceRegistrationService;
    private final ChallengeService challengeService;
    
    /**
     * Register a new device for the current user.
     * This stores the device's public key and returns a device token.
     */
    @PostMapping("/register")
    public ResponseEntity<DeviceRegistrationResponseDTO> registerDevice(
            @Valid @RequestBody DeviceRegistrationRequestDTO request) {
        log.info("Device registration request for deviceId: {}", request.getDeviceId());
        DeviceRegistrationResponseDTO response = deviceRegistrationService.registerDevice(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all registered devices for the current user.
     */
    @GetMapping
    public ResponseEntity<List<DeviceRegistrationDTO>> getMyDevices() {
        return ResponseEntity.ok(deviceRegistrationService.getMyDevices());
    }
    
    /**
     * Get device information by device token.
     */
    @GetMapping("/by-token/{deviceToken}")
    public ResponseEntity<DeviceRegistrationDTO> getDeviceByToken(@PathVariable String deviceToken) {
        return ResponseEntity.ok(deviceRegistrationService.getDeviceByToken(deviceToken));
    }
    
    /**
     * Check if a device is trusted.
     */
    @GetMapping("/trusted/{deviceToken}")
    public ResponseEntity<Boolean> isDeviceTrusted(@PathVariable String deviceToken) {
        return ResponseEntity.ok(deviceRegistrationService.isDeviceTrusted(deviceToken));
    }
    
    /**
     * Unregister a device.
     */
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> unregisterDevice(@PathVariable String deviceId) {
        log.info("Unregistering device: {}", deviceId);
        deviceRegistrationService.unregisterDevice(deviceId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Update FCM token for push notifications.
     * Should be called when:
     * - App starts and gets a new FCM token
     * - FCM token is refreshed by Firebase
     */
    @PutMapping("/fcm-token")
    public ResponseEntity<Void> updateFcmToken(
            @RequestHeader("X-Device-Token") String deviceToken,
            @Valid @RequestBody UpdateFcmTokenRequestDTO request) {
        log.info("Updating FCM token for device");
        deviceRegistrationService.updateFcmToken(deviceToken, request.getFcmToken());
        return ResponseEntity.ok().build();
    }
    
    // ==================== Challenge-Response Endpoints ====================
    
    /**
     * Get the current pending challenge for a device.
     * If no pending challenge exists, a new one is generated.
     */
    @GetMapping("/challenges/current")
    public ResponseEntity<ChallengeDTO> getCurrentChallenge(
            @RequestHeader("X-Device-Token") String deviceToken) {
        log.debug("Getting current challenge for device token: {}", deviceToken);
        ChallengeDTO challenge = challengeService.getCurrentChallenge(deviceToken);
        return ResponseEntity.ok(challenge);
    }
    
    /**
     * Generate a new challenge for the device.
     */
    @PostMapping("/challenges/generate")
    public ResponseEntity<ChallengeDTO> generateChallenge(
            @RequestHeader("X-Device-Token") String deviceToken) {
        log.debug("Generating new challenge for device token: {}", deviceToken);
        ChallengeDTO challenge = challengeService.generateChallenge(deviceToken);
        return ResponseEntity.ok(challenge);
    }
    
    /**
     * Submit a response to a challenge.
     */
    @PostMapping("/challenges/{challengeId}/respond")
    public ResponseEntity<ChallengeResponseResultDTO> respondToChallenge(
            @PathVariable Long challengeId,
            @Valid @RequestBody ChallengeResponseRequestDTO response) {
        log.debug("Processing challenge response for challenge: {}", challengeId);
        ChallengeResponseResultDTO result = challengeService.processResponse(challengeId, response);
        return ResponseEntity.ok(result);
    }
}
