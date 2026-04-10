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
public class VerifyAdvertisementRequestDTO {
    
    @NotNull(message = "Advertisement ID is required")
    private Long advertisementId;
    
    // Hash of the current wallpaper (optional, for extra verification)
    private String wallpaperHash;
    
    // Whether the wallpaper matches
    @NotNull(message = "isValid is required")
    private Boolean isValid;
    
    // Total seconds the wallpaper has been displayed (tracked by mobile app)
    private Long totalValidSeconds;
}
