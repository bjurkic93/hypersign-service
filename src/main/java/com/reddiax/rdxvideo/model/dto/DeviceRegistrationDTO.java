package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeviceRegistrationDTO {
    private Long id;
    private Long userId;
    private String deviceId;
    private String deviceToken;
    private String deviceModel;
    private String deviceManufacturer;
    private String osVersion;
    private String appVersion;
    private LocalDateTime registeredAt;
    private LocalDateTime lastSeenAt;
    private Boolean isTrusted;
    private Integer trustScore;
}
