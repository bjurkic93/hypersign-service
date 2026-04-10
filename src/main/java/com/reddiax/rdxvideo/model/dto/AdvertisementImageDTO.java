package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvertisementImageDTO {
    private Long id;
    private Long imageId;
    private String imageUrl;
    private Integer displayOrder;
    private Integer displayDurationSeconds;
    private Integer displayDurationDays;
    
    /**
     * SHA-256 hash of the image content.
     * Used for verifying wallpaper integrity on mobile devices.
     */
    private String imageHash;
}
