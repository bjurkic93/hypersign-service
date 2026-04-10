package com.reddiax.rdxvideo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity for TV device registration.
 * Unlike mobile devices which are tied to users, TV devices are tied to organizations.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "TV_DEVICE_REGISTRATION")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TvDeviceRegistrationEntity extends Auditable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @Column(name = "device_id", nullable = false, unique = true, length = 64)
    private String deviceId;

    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "device_token", nullable = false, unique = true, length = 128)
    private String deviceToken;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "device_model", length = 100)
    private String deviceModel;

    @Column(name = "device_manufacturer", length = 100)
    private String deviceManufacturer;

    @Column(name = "os_version", length = 20)
    private String osVersion;

    @Column(name = "app_version", length = 20)
    private String appVersion;

    @Column(name = "registered_at", nullable = false)
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "is_trusted", nullable = false)
    @Builder.Default
    private Boolean isTrusted = true;

    @Column(name = "trust_score", nullable = false)
    @Builder.Default
    private Integer trustScore = 100;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /** Zahtjev za pokretanje aplikacije s APIja – postavi se kad netko pozove request-launch. */
    @Column(name = "launch_requested_at")
    private LocalDateTime launchRequestedAt;

    /** Kad je aplikacija zadnji put javila da je u foregroundu (za label "aplikacija upaljena" u adminu). */
    @Column(name = "last_foreground_at")
    private LocalDateTime lastForegroundAt;

    /** Je li HDMI/display povezan na Android boxu – slika se projicira (true) ili ne (false). Null = unknown / nije Android box. */
    @Column(name = "display_connected")
    private Boolean displayConnected;

    /**
     * Decrease trust score by a given amount.
     * Trust score cannot go below 0.
     */
    public void decreaseTrustScore(int amount) {
        this.trustScore = Math.max(0, this.trustScore - amount);
        if (this.trustScore < 50) {
            this.isTrusted = false;
        }
    }
    
    /**
     * Increase trust score by a given amount.
     * Trust score cannot exceed 100.
     */
    public void increaseTrustScore(int amount) {
        this.trustScore = Math.min(100, this.trustScore + amount);
        if (this.trustScore >= 50) {
            this.isTrusted = true;
        }
    }
    
    /**
     * Update last seen timestamp.
     */
    public void updateLastSeen() {
        this.lastSeenAt = LocalDateTime.now();
    }
}
