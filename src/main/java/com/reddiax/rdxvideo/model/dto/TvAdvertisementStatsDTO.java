package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for TV advertisement performance stats.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TvAdvertisementStatsDTO {
    
    private Long advertisementId;
    private String advertisementName;
    private Long totalImpressions;
    private Long totalDurationSeconds;
    private BigDecimal totalAmountCharged;
    private Long uniqueDevices;
    private Boolean isPublic;
}
