package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for TV device registration.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TvDeviceRegistrationRequestDTO {
    
    @NotBlank(message = "Device ID is required")
    @Size(max = 64, message = "Device ID must not exceed 64 characters")
    private String deviceId;
    
    @NotBlank(message = "Public key is required")
    private String publicKey;
    
    @NotNull(message = "Organization ID is required")
    private Long organizationId;
    
    @Size(max = 100, message = "Device name must not exceed 100 characters")
    private String deviceName;
    
    @Size(max = 100, message = "Device model must not exceed 100 characters")
    private String deviceModel;
    
    @Size(max = 100, message = "Device manufacturer must not exceed 100 characters")
    private String deviceManufacturer;
    
    @Size(max = 20, message = "OS version must not exceed 20 characters")
    private String osVersion;
    
    @Size(max = 20, message = "App version must not exceed 20 characters")
    private String appVersion;
}
