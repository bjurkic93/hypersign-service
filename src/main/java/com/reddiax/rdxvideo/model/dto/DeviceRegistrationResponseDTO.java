package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeviceRegistrationResponseDTO {
    private Long id;
    private String deviceToken;
    private String serverPublicKey;
    private Boolean isRegistered;
    private String message;
}
