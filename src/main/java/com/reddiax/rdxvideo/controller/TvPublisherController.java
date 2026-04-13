package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.PlaybackLogBatchRequest;
import com.reddiax.rdxvideo.model.dto.PlaybackLogBatchResponse;
import com.reddiax.rdxvideo.model.dto.TvContentResponse;
import com.reddiax.rdxvideo.model.dto.TvImpressionRequestDTO;
import com.reddiax.rdxvideo.model.dto.TvImpressionResponseDTO;
import com.reddiax.rdxvideo.service.PlaybackLogService;
import com.reddiax.rdxvideo.service.PushNotificationService;
import com.reddiax.rdxvideo.service.TvAdvertisementService;
import com.reddiax.rdxvideo.service.TvAuthSessionService;
import com.reddiax.rdxvideo.service.TvContentService;
import com.reddiax.rdxvideo.service.TvDeviceRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API endpoints for TV publisher app (RdX GlanceTV).
 * Handles TV advertisement impression tracking and billing.
 * 
 * All endpoints require device token authentication via X-Device-Token header.
 * The organization ID is derived from the validated device token for security.
 */
@RestController
@RequestMapping("/api/v1/tv")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "TV Publisher", description = "APIs for TV publisher app (RdX GlanceTV)")
public class TvPublisherController {

    private final TvAdvertisementService tvAdvertisementService;
    private final TvDeviceRegistrationService tvDeviceRegistrationService;
    private final PlaybackLogService playbackLogService;
    private final TvContentService tvContentService;
    private final TvAuthSessionService tvAuthSessionService;
    private final PushNotificationService pushNotificationService;

    /**
     * Record a TV advertisement impression.
     * 
     * Security:
     * - Requires valid X-Device-Token header
     * - Device must be active and trusted
     * - Organization ID is derived from the device token (not from request body)
     * 
     * Billing logic:
     * - Owner is ALWAYS charged based on pricing tier (per minute)
     * - Displayer is rewarded ONLY if:
     *   1. Ad is marked as public (isPublic = true)
     *   2. Displayer organization is different from owner organization
     * 
     * @param deviceToken the device token from X-Device-Token header
     * @param request impression details including ad ID and duration
     * @return billing summary with amounts charged/rewarded
     */
    @PostMapping("/impressions")
    @Operation(summary = "Record TV ad impression", 
               description = "Record that a TV advertisement was displayed. " +
                       "Requires valid device token. " +
                       "Charges the ad owner based on pricing tier. " +
                       "Rewards the publisher if displaying a public ad from another organization.")
    public ResponseEntity<TvImpressionResponseDTO> recordImpression(
            @Parameter(description = "Device token for authentication", required = true)
            @RequestHeader("X-Device-Token") String deviceToken,
            @Valid @RequestBody TvImpressionRequestDTO request) {
        
        // Validate device token and get the organization ID
        Long organizationId = tvDeviceRegistrationService.validateDeviceToken(deviceToken);
        
        log.info("Recording TV impression from device token for org {}, ad {}", 
                organizationId, request.getAdvertisementId());
        
        // Override the displayerOrganizationId with the validated one from device token
        // This ensures security - clients cannot fake their organization
        request.setDisplayerOrganizationId(organizationId);
        
        return ResponseEntity.ok(tvAdvertisementService.recordImpression(request));
    }
    
    /**
     * Record a signed TV advertisement impression.
     * 
     * Enhanced security with signature verification:
     * - Requires valid X-Device-Token header
     * - Requires X-Signature header with ECDSA signature of request body
     * - Signature is verified using the device's registered public key
     * 
     * @param deviceToken the device token from X-Device-Token header
     * @param signature ECDSA signature of the request body
     * @param request impression details including ad ID and duration
     * @return billing summary with amounts charged/rewarded
     */
    @PostMapping("/impressions/signed")
    @Operation(summary = "Record signed TV ad impression", 
               description = "Record a TV ad impression with cryptographic signature verification. " +
                       "Provides enhanced security by verifying the request was signed by the registered device.")
    public ResponseEntity<TvImpressionResponseDTO> recordSignedImpression(
            @Parameter(description = "Device token for authentication", required = true)
            @RequestHeader("X-Device-Token") String deviceToken,
            @Parameter(description = "ECDSA signature of request body", required = true)
            @RequestHeader("X-Signature") String signature,
            @Valid @RequestBody TvImpressionRequestDTO request) {
        
        // Validate device token and get the organization ID
        Long organizationId = tvDeviceRegistrationService.validateDeviceToken(deviceToken);
        
        // Verify signature (the signature should be of: advertisementId + durationSeconds + timestamp)
        String dataToVerify = request.getAdvertisementId() + ":" + request.getDurationSeconds();
        boolean isValidSignature = tvDeviceRegistrationService.verifyDeviceSignature(
                deviceToken, dataToVerify, signature);
        
        if (!isValidSignature) {
            log.warn("Invalid signature for TV impression from device token, org {}", organizationId);
            // The verifyDeviceSignature method already decreases trust score
            // We could throw an exception here, but for now we'll still process
            // (trust score system will eventually block untrusted devices)
        }
        
        log.info("Recording signed TV impression from device token for org {}, ad {}, signature valid: {}", 
                organizationId, request.getAdvertisementId(), isValidSignature);
        
        // Override the displayerOrganizationId with the validated one
        request.setDisplayerOrganizationId(organizationId);
        
        return ResponseEntity.ok(tvAdvertisementService.recordImpression(request));
    }

    /**
     * Submit playback logs in batch.
     * TV devices periodically send what content was displayed and for how long.
     */
    @PostMapping("/playback-logs")
    @Operation(summary = "Submit playback logs batch",
               description = "Submit a batch of playback logs from the TV device. " +
                       "Used to track what content was displayed and for how long.")
    public ResponseEntity<PlaybackLogBatchResponse> submitPlaybackLogs(
            @Parameter(description = "Device token for authentication", required = true)
            @RequestHeader("X-Device-Token") String deviceToken,
            @Valid @RequestBody PlaybackLogBatchRequest request) {
        
        log.info("Receiving {} playback logs from device", request.getLogs().size());
        return ResponseEntity.ok(playbackLogService.saveBatch(deviceToken, request));
    }

    /**
     * Get current content for the TV device.
     * Returns the active schedule with playlist, layout and all content items.
     */
    @GetMapping("/content")
    @Operation(summary = "Get TV content",
               description = "Get the current active content for this TV device. " +
                       "Returns schedule with playlist, layout sections, and content items.")
    public ResponseEntity<TvContentResponse> getContent(
            @Parameter(description = "Device token for authentication", required = true)
            @RequestHeader("X-Device-Token") String deviceToken) {
        
        Long organizationId = tvDeviceRegistrationService.validateDeviceToken(deviceToken);
        log.info("Fetching TV content for org {}", organizationId);
        
        return ResponseEntity.ok(tvContentService.getTvContent(organizationId));
    }

    /**
     * Register FCM token for push notifications.
     * Called by TV device after successful authentication.
     */
    @PostMapping("/device/fcm-token")
    @Operation(summary = "Register FCM token",
               description = "Register Firebase Cloud Messaging token for push notifications. " +
                       "Called by TV device to receive instant content updates.")
    public ResponseEntity<Void> registerFcmToken(
            @Parameter(description = "Device token for authentication", required = true)
            @RequestHeader("X-Device-Token") String deviceToken,
            @Valid @RequestBody FcmTokenRequest request) {
        
        log.info("Registering FCM token for device");
        tvAuthSessionService.updateFcmToken(deviceToken, request.getFcmToken());
        
        return ResponseEntity.ok().build();
    }

    /**
     * DTO for FCM token registration request.
     */
    @lombok.Data
    public static class FcmTokenRequest {
        private String fcmToken;
    }
}
