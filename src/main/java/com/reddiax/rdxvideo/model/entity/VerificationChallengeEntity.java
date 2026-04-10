package com.reddiax.rdxvideo.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "VERIFICATION_CHALLENGE")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationChallengeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private DeviceRegistrationEntity device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_advertisement_id")
    private UserAdvertisementEntity userAdvertisement;

    @Column(name = "nonce", nullable = false, length = 64)
    private String nonce;

    @Column(name = "challenge_data", nullable = false, columnDefinition = "TEXT")
    private String challengeData;

    @Column(name = "expected_wallpaper_hash", length = 64)
    private String expectedWallpaperHash;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "response_signature", columnDefinition = "TEXT")
    private String responseSignature;

    @Column(name = "response_wallpaper_hash", length = 64)
    private String responseWallpaperHash;

    @Column(name = "response_valid")
    private Boolean responseValid;

    @Column(name = "wallpaper_hash_match")
    private Boolean wallpaperHashMatch;

    @Column(name = "device_attestation_token", columnDefinition = "TEXT")
    private String deviceAttestationToken;

    @Column(name = "attestation_valid")
    private Boolean attestationValid;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
    
    /**
     * Check if the challenge has expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if the challenge has been responded to.
     */
    public boolean isResponded() {
        return respondedAt != null;
    }
    
    /**
     * Check if challenge is still pending (not responded and not expired).
     */
    public boolean isPending() {
        return !isResponded() && !isExpired();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }
}
