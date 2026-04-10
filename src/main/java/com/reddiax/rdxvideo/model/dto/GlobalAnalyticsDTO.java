package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalAnalyticsDTO {
    // User statistics
    private Long totalActiveUsers;        // Currently showing any ad
    private Long totalUniqueUsers;        // Total unique users who have ever shown an ad
    private Long totalCompletedSessions;  // Total completed ad sessions
    private Long totalViolatedSessions;   // Total violated sessions
    private Long totalActiveSessions;     // All currently active sessions
    
    // Rates
    private Double overallCompletionRate; // completedSessions / totalSessions * 100
    private Double overallViolationRate;  // violatedSessions / totalSessions * 100
    
    // Financial
    private BigDecimal totalRewardsDistributed;
    
    // Advertisement stats
    private Long totalActiveAdvertisements;   // Ads that are currently being shown
    private Long totalAdvertisements;         // All ads in the system
}
