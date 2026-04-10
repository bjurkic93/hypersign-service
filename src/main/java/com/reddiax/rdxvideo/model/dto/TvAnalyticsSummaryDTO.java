package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Summary DTO for TV analytics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TvAnalyticsSummaryDTO {
    
    private Long totalImpressions;
    private Long totalDurationSeconds;
    private BigDecimal totalAmountCharged;
    private BigDecimal totalAmountEarned;
    private Long activeDevices;
    private Long activeAdvertisements;
}
