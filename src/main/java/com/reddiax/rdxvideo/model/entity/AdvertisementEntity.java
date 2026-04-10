package com.reddiax.rdxvideo.model.entity;

import com.reddiax.rdxvideo.constant.AdvertisementMediaTypeEnum;
import com.reddiax.rdxvideo.constant.AdvertisementPlatformEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ADVERTISEMENT")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AdvertisementEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "organization_id")
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private AdvertisementMediaTypeEnum mediaType;

    @Column(name = "video_id")
    private Long videoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdvertisementPlatformEnum platform;

    @Column(name = "pricing_tier_id")
    private Long pricingTierId;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
