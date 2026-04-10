package com.reddiax.rdxvideo.model.entity;

import com.reddiax.rdxvideo.constant.UserAdvertisementStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Tracks the relationship between users and advertisements.
 * Records when users activate ads and viewing time.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "USER_ADVERTISEMENT", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "advertisement_id"}))
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserAdvertisementEntity extends Auditable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertisement_id", nullable = false)
    private AdvertisementEntity advertisement;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserAdvertisementStatusEnum status = UserAdvertisementStatusEnum.ACTIVE;

    @Column(name = "activated_at", nullable = false)
    private LocalDateTime activatedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "verification_count", nullable = false)
    @Builder.Default
    private Integer verificationCount = 0;

    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    @Column(name = "total_valid_seconds", nullable = false)
    @Builder.Default
    private Long totalValidSeconds = 0L;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "deactivation_reason", length = 100)
    private String deactivationReason;

    public boolean isActive() {
        return status == UserAdvertisementStatusEnum.ACTIVE;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void recordVerification() {
        this.verificationCount++;
        this.lastVerifiedAt = LocalDateTime.now();
    }

    public void recordVerification(Long totalSeconds) {
        this.verificationCount++;
        this.lastVerifiedAt = LocalDateTime.now();
        if (totalSeconds != null && totalSeconds > 0) {
            this.totalValidSeconds = totalSeconds;
        }
    }

    public void complete() {
        this.status = UserAdvertisementStatusEnum.COMPLETED;
        this.deactivatedAt = LocalDateTime.now();
        this.deactivationReason = "Completed successfully";
    }

    public void markViolation(String reason) {
        this.status = UserAdvertisementStatusEnum.VIOLATED;
        this.deactivatedAt = LocalDateTime.now();
        this.deactivationReason = reason;
    }

    public void expire() {
        this.status = UserAdvertisementStatusEnum.EXPIRED;
        this.deactivatedAt = LocalDateTime.now();
        this.deactivationReason = "Duration expired";
    }
}
