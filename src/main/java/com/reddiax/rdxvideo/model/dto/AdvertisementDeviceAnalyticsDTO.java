package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for per-device analytics of an advertisement.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvertisementDeviceAnalyticsDTO {
    private Long userAdvertisementId;
    private String deviceInfo;
    private Long totalVerifications;
    private Long totalSeconds;
    private LocalDateTime lastVerifiedAt;
}
