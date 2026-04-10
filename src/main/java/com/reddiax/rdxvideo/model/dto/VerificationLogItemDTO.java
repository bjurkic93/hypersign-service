package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for a single verification log item from mobile app.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationLogItemDTO {
    private Long userAdvertisementId;
    private Long timestamp; // milliseconds since epoch
    private String wallpaperHash;
    private Boolean hashMatch;
    private String verificationResult; // SUCCESS, HASH_MISMATCH, NOT_SET, ERROR
    private String deviceState; // JSON with battery, screen state, etc.
}
