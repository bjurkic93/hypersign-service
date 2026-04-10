package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.TvImpressionRequestDTO;
import com.reddiax.rdxvideo.model.dto.TvImpressionResponseDTO;

/**
 * Service for handling TV advertisement operations.
 * 
 * TV ads have different billing logic than mobile:
 * - Owner always pays based on pricing tier
 * - Publisher earns only when displaying OTHER organization's public ads
 */
public interface TvAdvertisementService {

    /**
     * Record a TV advertisement impression and process billing.
     * 
     * Logic:
     * 1. ALWAYS charge the ad owner based on pricingTier (price per minute)
     * 2. IF ad is public AND displayer is different from owner:
     *    - Reward the displayer based on rewardTier (reward per minute)
     * 
     * @param request the impression details
     * @return response with billing summary
     */
    TvImpressionResponseDTO recordImpression(TvImpressionRequestDTO request);
}
