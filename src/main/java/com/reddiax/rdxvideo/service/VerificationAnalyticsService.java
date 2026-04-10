package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.AdvertisementDeviceAnalyticsDTO;
import com.reddiax.rdxvideo.model.dto.AdvertisementSummaryDTO;
import com.reddiax.rdxvideo.model.dto.AdvertisementTimelineDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for advertisement verification analytics.
 */
public interface VerificationAnalyticsService {

    /**
     * Get timeline analytics for an advertisement.
     * 
     * @param advertisementId The advertisement ID
     * @param startDate Start of the time range
     * @param endDate End of the time range
     * @param interval Aggregation interval (HOUR or DAY)
     * @return List of timeline data points
     */
    List<AdvertisementTimelineDTO> getTimeline(Long advertisementId, LocalDateTime startDate, LocalDateTime endDate, String interval);

    /**
     * Get per-device analytics for an advertisement.
     * 
     * @param advertisementId The advertisement ID
     * @param limit Maximum number of devices to return
     * @return List of device analytics
     */
    List<AdvertisementDeviceAnalyticsDTO> getDeviceAnalytics(Long advertisementId, int limit);

    /**
     * Get summary statistics for an advertisement.
     * 
     * @param advertisementId The advertisement ID
     * @return Summary statistics
     */
    AdvertisementSummaryDTO getSummary(Long advertisementId);
}
