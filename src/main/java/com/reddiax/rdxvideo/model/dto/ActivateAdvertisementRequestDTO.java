package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivateAdvertisementRequestDTO {
    
    @NotNull(message = "Advertisement ID is required")
    private Long advertisementId;
    
    // User's selected reward tier (for ads with multiple reward options)
    private Long rewardTierId;
    
    // Optional device info for analytics
    private String deviceModel;
    private String deviceManufacturer;
    private String osVersion;
    private String appVersion;
}
