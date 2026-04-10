package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for advertisement video items.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvertisementVideoDTO {
    
    private Long id;
    
    private Long advertisementId;
    
    private Long videoId;
    
    private String videoUrl;
    
    private Integer displayOrder;
    
    private Integer displayDurationSeconds;
    
    private String title;
}
