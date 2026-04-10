package com.reddiax.rdxvideo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entity for advertisement videos.
 * Supports multiple videos per advertisement with display order and duration.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ADVERTISEMENT_VIDEO")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AdvertisementVideoEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "advertisement_id", nullable = false)
    private Long advertisementId;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * Display duration in seconds.
     * If null, the full video length is used.
     */
    @Column(name = "display_duration_seconds")
    private Integer displayDurationSeconds;

    /**
     * Title/label for this video in the playlist.
     */
    @Column(name = "title", length = 255)
    private String title;
}
