package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeviceRegistrationRequestDTO {
    
    @NotBlank(message = "Device ID is required")
    @Size(max = 64, message = "Device ID must not exceed 64 characters")
    private String deviceId;
    
    @NotBlank(message = "Public key is required")
    private String publicKey;
    
    @Size(max = 100, message = "Device model must not exceed 100 characters")
    private String deviceModel;
    
    @Size(max = 100, message = "Device manufacturer must not exceed 100 characters")
    private String deviceManufacturer;
    
    @Size(max = 20, message = "OS version must not exceed 20 characters")
    private String osVersion;
    
    @Size(max = 20, message = "App version must not exceed 20 characters")
    private String appVersion;
    
    @Size(max = 500, message = "FCM token must not exceed 500 characters")
    private String fcmToken;
}
