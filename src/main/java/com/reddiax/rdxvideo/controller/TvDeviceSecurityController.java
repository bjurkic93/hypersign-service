package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.TvDeviceRegistrationDTO;
import com.reddiax.rdxvideo.model.dto.TvDeviceRegistrationRequestDTO;
import com.reddiax.rdxvideo.model.dto.TvDeviceRegistrationResponseDTO;
import com.reddiax.rdxvideo.service.TvDeviceRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for TV device security operations.
 * Handles device registration, authentication, and management for TV devices.
 */
@RestController
@RequestMapping("/api/v1/tv/devices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "TV Device Security", description = "APIs for TV device registration and security")
public class TvDeviceSecurityController {
    
    private final TvDeviceRegistrationService tvDeviceRegistrationService;
    
    /**
     * Register a new TV device for an organization.
     * This stores the device's public key and returns a device token.
     * 
     * The device token should be used in subsequent requests via X-Device-Token header.
     */
    @PostMapping("/register")
    @Operation(summary = "Register TV device", 
               description = "Register a new TV device for an organization. Returns a device token for authentication.")
    public ResponseEntity<TvDeviceRegistrationResponseDTO> registerDevice(
            @Valid @RequestBody TvDeviceRegistrationRequestDTO request) {
        log.info("TV device registration request for deviceId: {}, orgId: {}", 
                request.getDeviceId(), request.getOrganizationId());
        TvDeviceRegistrationResponseDTO response = tvDeviceRegistrationService.registerDevice(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all registered TV devices. Optional filter by organization. SYSTEM_ADMIN only.
     */
    @GetMapping
    @Operation(summary = "Get all TV devices", 
               description = "Get all registered TV devices. Optionally filter by organizationId.")
    public ResponseEntity<List<TvDeviceRegistrationDTO>> getAllDevices(
            @RequestParam(required = false) Long organizationId) {
        return ResponseEntity.ok(tvDeviceRegistrationService.getAllDevices(organizationId));
    }

    /**
     * Get all registered TV devices for an organization. SYSTEM_ADMIN only.
     */
    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get organization TV devices", 
               description = "Get all registered TV devices for a specific organization")
    public ResponseEntity<List<TvDeviceRegistrationDTO>> getDevicesByOrganization(
            @PathVariable Long organizationId) {
        return ResponseEntity.ok(tvDeviceRegistrationService.getDevicesByOrganization(organizationId));
    }
    
    /**
     * Get device information by device token.
     * @param foreground when true, records that the app is in foreground (for "aplikacija upaljena" in admin)
     */
    @GetMapping("/by-token")
    @Operation(summary = "Get device by token", 
               description = "Get TV device information using the device token. Pass foreground=true when app is in foreground.")
    public ResponseEntity<TvDeviceRegistrationDTO> getDeviceByToken(
            @RequestHeader("X-Device-Token") String deviceToken,
            @RequestParam(required = false) Boolean foreground) {
        return ResponseEntity.ok(tvDeviceRegistrationService.getDeviceByToken(deviceToken, foreground));
    }
    
    /**
     * Check if a TV device is trusted.
     */
    @GetMapping("/trusted")
    @Operation(summary = "Check device trust status", 
               description = "Check if the TV device is trusted based on its trust score")
    public ResponseEntity<Boolean> isDeviceTrusted(
            @RequestHeader("X-Device-Token") String deviceToken) {
        return ResponseEntity.ok(tvDeviceRegistrationService.isDeviceTrusted(deviceToken));
    }
    
    /**
     * Validate device token and return organization ID.
     * Used internally or for health checks.
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate device token", 
               description = "Validate the device token and return the associated organization ID")
    public ResponseEntity<Long> validateDevice(
            @RequestHeader("X-Device-Token") String deviceToken) {
        Long organizationId = tvDeviceRegistrationService.validateDeviceToken(deviceToken);
        return ResponseEntity.ok(organizationId);
    }
    
    /**
     * Unregister (deactivate) a TV device. SYSTEM_ADMIN only.
     */
    @DeleteMapping("/{deviceId}")
    @Operation(summary = "Unregister TV device", 
               description = "Deactivate a TV device for an organization")
    public ResponseEntity<Void> unregisterDevice(
            @PathVariable String deviceId,
            @RequestParam Long organizationId) {
        log.info("Unregistering TV device: {} for org: {}", deviceId, organizationId);
        tvDeviceRegistrationService.unregisterDevice(deviceId, organizationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Zahtjev za pokretanje aplikacije na uređaju (s APIja). SYSTEM_ADMIN only.
     * Uređaj pri sljedećem pollu (GET by-token) vidi launchRequestedAt i može otvoriti app.
     */
    @PostMapping("/request-launch")
    @Operation(summary = "Request app launch on device",
               description = "Set a flag so the TV device will open the app on next poll (or when it wakes).")
    public ResponseEntity<Void> requestLaunch(
            @RequestParam String deviceId,
            @RequestParam Long organizationId) {
        tvDeviceRegistrationService.requestLaunch(deviceId, organizationId);
        return ResponseEntity.ok().build();
    }

    /**
     * Ukloni zahtjev za pokretanje. Poziva uređaj nakon što otvori aplikaciju (X-Device-Token).
     */
    @PostMapping("/clear-launch")
    @Operation(summary = "Clear launch request",
               description = "Called by the device after it has opened the app (device token auth).")
    public ResponseEntity<Void> clearLaunchRequest(
            @RequestHeader("X-Device-Token") String deviceToken) {
        tvDeviceRegistrationService.clearLaunchRequest(deviceToken);
        return ResponseEntity.ok().build();
    }

    /**
     * Javi stanje HDMI/displaya (Android box) – projicira li se slika ili ne.
     */
    @PostMapping("/display-status")
    @Operation(summary = "Report display/HDMI status",
               description = "Called by the TV app when HDMI/display connection state changes (Android box only).")
    public ResponseEntity<Void> updateDisplayStatus(
            @RequestHeader("X-Device-Token") String deviceToken,
            @RequestParam boolean connected) {
        tvDeviceRegistrationService.updateDisplayConnected(deviceToken, connected);
        return ResponseEntity.ok().build();
    }
}
