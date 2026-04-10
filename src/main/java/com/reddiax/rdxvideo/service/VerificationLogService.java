package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.VerificationLogSyncRequestDTO;
import com.reddiax.rdxvideo.model.dto.VerificationLogSyncResponseDTO;

/**
 * Service for handling advertisement verification logs.
 */
public interface VerificationLogService {

    /**
     * Sync verification logs from mobile app.
     * 
     * @param userId The authenticated user's ID
     * @param request The sync request containing logs
     * @return Response with sync status
     */
    VerificationLogSyncResponseDTO syncLogs(Long userId, VerificationLogSyncRequestDTO request);
}
