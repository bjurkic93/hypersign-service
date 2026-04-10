package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.ChallengeDTO;
import com.reddiax.rdxvideo.model.dto.ChallengeResponseRequestDTO;
import com.reddiax.rdxvideo.model.dto.ChallengeResponseResultDTO;

public interface ChallengeService {
    
    /**
     * Generate a new challenge for the device.
     * The challenge is encrypted with the device's public key.
     */
    ChallengeDTO generateChallenge(String deviceToken);
    
    /**
     * Get the current pending challenge for a device.
     * If no pending challenge exists, generates a new one.
     */
    ChallengeDTO getCurrentChallenge(String deviceToken);
    
    /**
     * Process a challenge response from the device.
     * Verifies the signature and wallpaper hash.
     */
    ChallengeResponseResultDTO processResponse(Long challengeId, ChallengeResponseRequestDTO response);
    
    /**
     * Verify a digital signature using the device's public key.
     */
    boolean verifySignature(String deviceToken, byte[] data, String signature);
    
    /**
     * Clean up expired challenges.
     */
    void cleanupExpiredChallenges();
}
