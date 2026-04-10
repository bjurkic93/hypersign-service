package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChallengeResponseRequestDTO {
    
    @NotBlank(message = "Nonce is required")
    private String nonce;
    
    @NotBlank(message = "Wallpaper hash is required")
    private String wallpaperHash;
    
    @NotNull(message = "Timestamp is required")
    private Long timestamp;
    
    @NotBlank(message = "Signature is required")
    private String signature;
    
    private String deviceAttestationToken;
}
