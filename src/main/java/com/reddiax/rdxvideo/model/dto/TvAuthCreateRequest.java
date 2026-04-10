package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to create a new TV auth session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TvAuthCreateRequest {
    
    @NotBlank(message = "Device ID is required")
    private String deviceId;
    
    private String deviceName;
    private String deviceModel;
    private String deviceManufacturer;
}
