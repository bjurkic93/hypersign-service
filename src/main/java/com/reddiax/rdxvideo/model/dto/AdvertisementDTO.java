package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.AdvertisementMediaTypeEnum;
import com.reddiax.rdxvideo.constant.AdvertisementPlatformEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvertisementDTO {
    private Long id;
    private String name;
    private String description;
    private Long organizationId;
    private String organizationName;
    private AdvertisementMediaTypeEnum mediaType;
    
    // Legacy single video (for backward compatibility)
    private Long videoId;
    private String videoUrl;
    
    // Multiple videos (for VIDEO_SET and MIXED_PLAYLIST types)
    private List<AdvertisementVideoDTO> videos;
    
    // Multiple images (for IMAGE_SET and MIXED_PLAYLIST types)
    private List<AdvertisementImageDTO> images;
    private AdvertisementPlatformEnum platform;
    private Long pricingTierId;
    private PricingTierDTO pricingTier;
    private Boolean isPublic;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
