package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.TvAuthApproveRequest;
import com.reddiax.rdxvideo.model.dto.TvAuthCreateRequest;
import com.reddiax.rdxvideo.model.dto.TvAuthSessionDTO;

/**
 * Service for TV QR code authentication flow.
 */
public interface TvAuthSessionService {
    
    /**
     * Create a new auth session for a TV device.
     * Returns session info including QR code URL.
     */
    TvAuthSessionDTO createSession(TvAuthCreateRequest request);
    
    /**
     * Get session status (TV polls this).
     * Returns tokens if session is approved.
     */
    TvAuthSessionDTO getSessionStatus(String sessionId);
    
    /**
     * Get session info for approval page (by session ID or code).
     */
    TvAuthSessionDTO getSessionForApproval(String sessionIdOrCode);
    
    /**
     * Approve a session (user action from web).
     * Links TV to organization and generates tokens.
     */
    TvAuthSessionDTO approveSession(String sessionIdOrCode, TvAuthApproveRequest request);
    
    /**
     * Mark session as used after TV retrieves tokens.
     */
    void markSessionUsed(String sessionId);
    
    /**
     * Cleanup expired sessions.
     */
    void cleanupExpiredSessions();

    /**
     * Update FCM token for a device.
     * 
     * @param deviceToken the device access token (X-Device-Token header)
     * @param fcmToken the Firebase Cloud Messaging token
     */
    void updateFcmToken(String deviceToken, String fcmToken);
}
