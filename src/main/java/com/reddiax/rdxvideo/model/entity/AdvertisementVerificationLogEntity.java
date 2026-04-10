package com.reddiax.rdxvideo.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for tracking individual verification events.
 * Stores time-series data for analytics and anti-cheat purposes.
 */
@Entity
@Table(name = "ADVERTISEMENT_VERIFICATION_LOG")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvertisementVerificationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_advertisement_id", nullable = false)
    private UserAdvertisementEntity userAdvertisement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_registration_id")
    private DeviceRegistrationEntity deviceRegistration;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    @Column(name = "wallpaper_hash", length = 64)
    private String wallpaperHash;

    @Column(name = "hash_match", nullable = false)
    @Builder.Default
    private Boolean hashMatch = false;

    @Column(name = "verification_result", nullable = false, length = 20)
    private String verificationResult;

    @Column(name = "duration_seconds", nullable = false)
    @Builder.Default
    private Integer durationSeconds = 0;

    @Column(name = "device_state", columnDefinition = "TEXT")
    private String deviceState;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
