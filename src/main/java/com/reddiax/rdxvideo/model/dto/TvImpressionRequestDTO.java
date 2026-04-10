package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for recording a TV advertisement impression.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TvImpressionRequestDTO {

    /**
     * The advertisement ID being displayed
     */
    @NotNull(message = "Advertisement ID is required")
    private Long advertisementId;

    /**
     * The organization ID of the TV displaying the ad (publisher).
     * This is automatically set from the validated device token for security.
     * Do not set this field manually - it will be overwritten.
     */
    private Long displayerOrganizationId;

    /**
     * Duration in seconds the ad was displayed
     */
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 second")
    private Long durationSeconds;

    /**
     * Optional device identifier for tracking
     */
    private String deviceId;
}
