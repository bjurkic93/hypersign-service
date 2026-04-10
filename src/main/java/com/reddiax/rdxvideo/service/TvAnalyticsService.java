package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.TvAdvertisementStatsDTO;
import com.reddiax.rdxvideo.model.dto.TvAnalyticsSummaryDTO;
import com.reddiax.rdxvideo.model.dto.TvDeviceAnalyticsDTO;
import com.reddiax.rdxvideo.model.dto.TvImpressionTimelineDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for TV analytics.
 */
public interface TvAnalyticsService {
    
    /**
     * Get summary of TV analytics.
     */
    TvAnalyticsSummaryDTO getSummary();
    
    /**
     * Get device analytics.
     */
    List<TvDeviceAnalyticsDTO> getDeviceAnalytics(int limit);
    
    /**
     * Get impression timeline.
     */
    List<TvImpressionTimelineDTO> getTimeline(LocalDateTime startDate, LocalDateTime endDate, String interval);
    
    /**
     * Get advertisement stats.
     */
    List<TvAdvertisementStatsDTO> getAdvertisementStats(int limit);
}
