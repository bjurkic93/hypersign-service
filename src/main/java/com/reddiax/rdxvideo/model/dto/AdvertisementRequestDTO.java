package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.AdvertisementMediaTypeEnum;
import com.reddiax.rdxvideo.constant.AdvertisementPlatformEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvertisementRequestDTO {
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private Long organizationId;

    @NotNull(message = "Media type is required")
    private AdvertisementMediaTypeEnum mediaType;

    // Legacy single video ID (for backward compatibility with VIDEO type)
    private Long videoId;

    // Multiple videos (for VIDEO_SET and MIXED_PLAYLIST types)
    @Valid
    private List<AdvertisementVideoRequestDTO> videos;

    // Multiple images (for IMAGE_SET and MIXED_PLAYLIST types)
    @Valid
    private List<AdvertisementImageRequestDTO> images;

    @NotNull(message = "Platform is required")
    private AdvertisementPlatformEnum platform;

    private Long pricingTierId;

    private Long rewardTierId; // Primary/default reward tier (for backward compatibility)

    private List<Long> rewardTierIds; // Multiple reward tier options

    private Boolean isPublic;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid to date is required")
    private LocalDateTime validTo;

    private Boolean active;

    // Reserved budget for MOBILE platform (required for MOBILE ads)
    @Positive(message = "Reserved budget must be positive")
    private BigDecimal reservedBudget;
}
