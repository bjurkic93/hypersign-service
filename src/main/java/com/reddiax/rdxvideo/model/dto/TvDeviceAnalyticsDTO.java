package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for TV device analytics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TvDeviceAnalyticsDTO {
    
    private String deviceId;
    private String deviceName;
    private String deviceModel;
    private Long organizationId;
    private String organizationName;
    private Long totalImpressions;
    private Long totalDurationSeconds;
    private BigDecimal totalEarned;
    private LocalDateTime lastSeenAt;
    private Boolean isTrusted;
    private Integer trustScore;
}
