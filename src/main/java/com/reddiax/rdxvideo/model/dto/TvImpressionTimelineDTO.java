package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for TV impression timeline data point.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TvImpressionTimelineDTO {
    
    private LocalDateTime timestamp;
    private Long impressionCount;
    private Long totalDurationSeconds;
    private BigDecimal amountCharged;
    private BigDecimal amountEarned;
    private Long uniqueDevices;
}
