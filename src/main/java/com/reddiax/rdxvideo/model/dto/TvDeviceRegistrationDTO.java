package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for TV device registration information.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TvDeviceRegistrationDTO {
    
    private Long id;
    
    private Long organizationId;
    
    private String organizationName;
    
    private String deviceId;
    
    private String deviceToken;
    
    private String deviceName;
    
    private String deviceModel;
    
    private String deviceManufacturer;
    
    private String osVersion;
    
    private String appVersion;
    
    private LocalDateTime registeredAt;
    
    private LocalDateTime lastSeenAt;
    
    private Boolean isTrusted;
    
    private Integer trustScore;
    
    private Boolean active;

    /** Je li aplikacija trenutno upaljena (u foregroundu) – true ako je lastForegroundAt u zadnjih nekoliko minuta. */
    private Boolean appActive;

    /** Ako je postavljen, uređaj treba pokrenuti aplikaciju (odgovor na request-launch s APIja). */
    private LocalDateTime launchRequestedAt;

    /** Je li HDMI/display povezan (Android box projicira na ekran). Null = unknown. */
    private Boolean displayConnected;
}
