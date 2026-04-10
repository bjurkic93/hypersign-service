package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.TvAuthApproveRequest;
import com.reddiax.rdxvideo.model.dto.TvAuthCreateRequest;
import com.reddiax.rdxvideo.model.dto.TvAuthSessionDTO;
import com.reddiax.rdxvideo.service.TvAuthSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for TV QR code authentication.
 * Handles the flow: TV shows QR → User scans → User approves → TV gets token.
 */
@RestController
@RequestMapping("/api/v1/tv/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "TV QR Authentication", description = "APIs for TV QR code login flow")
public class TvAuthController {

    private final TvAuthSessionService tvAuthSessionService;

    /**
     * Create a new auth session (called by TV).
     * Returns session info including QR code URL and display code.
     * This endpoint is public - no auth required.
     */
    @PostMapping("/session")
    @Operation(summary = "Create TV auth session",
            description = "TV calls this to get a QR code for user to scan. Returns session ID, code, and QR URL.")
    public ResponseEntity<TvAuthSessionDTO> createSession(@Valid @RequestBody TvAuthCreateRequest request) {
        log.info("TV requesting new auth session for device: {}", request.getDeviceId());
        TvAuthSessionDTO session = tvAuthSessionService.createSession(request);
        return ResponseEntity.ok(session);
    }

    /**
     * Get session status (TV polls this).
     * Returns tokens when approved.
     * This endpoint is public - no auth required.
     */
    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get session status (TV polling)",
            description = "TV polls this endpoint to check if user has approved. Returns tokens when status is APPROVED.")
    public ResponseEntity<TvAuthSessionDTO> getSessionStatus(@PathVariable String sessionId) {
        TvAuthSessionDTO session = tvAuthSessionService.getSessionStatus(sessionId);
        return ResponseEntity.ok(session);
    }

    /**
     * Get session info for approval page (called by web frontend).
     * Requires authentication.
     */
    @GetMapping("/session/{sessionIdOrCode}/info")
    @Operation(summary = "Get session info for approval",
            description = "Web frontend calls this to show approval page. Requires user authentication.")
    public ResponseEntity<TvAuthSessionDTO> getSessionForApproval(@PathVariable String sessionIdOrCode) {
        TvAuthSessionDTO session = tvAuthSessionService.getSessionForApproval(sessionIdOrCode);
        return ResponseEntity.ok(session);
    }

    /**
     * Approve a session (called by authenticated user from web).
     * Links TV to organization and generates device token.
     */
    @PostMapping("/session/{sessionIdOrCode}/approve")
    @Operation(summary = "Approve TV auth session",
            description = "User approves the TV login, linking it to their organization.")
    public ResponseEntity<TvAuthSessionDTO> approveSession(
            @PathVariable String sessionIdOrCode,
            @Valid @RequestBody TvAuthApproveRequest request) {
        log.info("Approving TV auth session: {} for org: {}", sessionIdOrCode, request.getOrganizationId());
        TvAuthSessionDTO session = tvAuthSessionService.approveSession(sessionIdOrCode, request);
        return ResponseEntity.ok(session);
    }

    /**
     * Mark session as used (TV calls after retrieving tokens).
     */
    @PostMapping("/session/{sessionId}/used")
    @Operation(summary = "Mark session as used",
            description = "TV calls this after successfully retrieving and storing the token.")
    public ResponseEntity<Void> markSessionUsed(@PathVariable String sessionId) {
        tvAuthSessionService.markSessionUsed(sessionId);
        return ResponseEntity.ok().build();
    }
}
