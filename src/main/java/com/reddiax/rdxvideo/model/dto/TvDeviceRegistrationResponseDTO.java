package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for TV device registration.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TvDeviceRegistrationResponseDTO {
    
    private Long id;
    
    private String deviceToken;
    
    private String serverPublicKey;
    
    private Long organizationId;
    
    private Boolean isRegistered;
    
    private String message;
}
