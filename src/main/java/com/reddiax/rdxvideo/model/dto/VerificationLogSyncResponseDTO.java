package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for verification log sync endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationLogSyncResponseDTO {
    private Integer syncedCount;
    private Long lastSyncedTimestamp; // milliseconds since epoch
    private String message;
}
