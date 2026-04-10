package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for advertisement summary statistics.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvertisementSummaryDTO {
    private Long totalDisplayTime; // in seconds
    private Double averageDisplayTimePerDevice; // in seconds
    private Long activeDevices;
    private Long totalVerifications;
    private Long successfulVerifications;
    private Double successRate; // percentage (0-100)
}
