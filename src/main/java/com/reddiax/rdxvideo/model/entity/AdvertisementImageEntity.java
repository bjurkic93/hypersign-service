package com.reddiax.rdxvideo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ADVERTISEMENT_IMAGE")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AdvertisementImageEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "advertisement_id", nullable = false)
    private Long advertisementId;

    @Column(name = "image_id", nullable = false)
    private Long imageId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "display_duration_seconds")
    private Integer displayDurationSeconds;

    @Column(name = "display_duration_days")
    private Integer displayDurationDays;

    /**
     * SHA-256 hash of the image content.
     * Used for verifying wallpaper integrity on mobile devices.
     */
    @Column(name = "image_hash", length = 64)
    private String imageHash;
}
