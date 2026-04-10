package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for a single point in the advertisement timeline.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvertisementTimelineDTO {
    private LocalDateTime timestamp;
    private Long verificationCount;
    private Long successCount;
    private Long failCount;
    private Long uniqueDevices;
}
