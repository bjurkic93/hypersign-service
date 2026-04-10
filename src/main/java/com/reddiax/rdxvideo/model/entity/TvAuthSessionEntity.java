package com.reddiax.rdxvideo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity for TV QR code authentication sessions.
 * TV generates a session, user scans QR, approves on web, TV gets tokens.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "TV_AUTH_SESSION")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TvAuthSessionEntity extends Auditable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique session code displayed to user (e.g., "ABC-123").
     * Short and human-readable for manual entry fallback.
     */
    @Column(name = "session_code", nullable = false, unique = true, length = 10)
    private String sessionCode;

    /**
     * Full session ID for QR code URL (longer, more secure).
     */
    @Column(name = "session_id", nullable = false, unique = true, length = 64)
    private String sessionId;

    /**
     * Device ID of the TV requesting auth.
     */
    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    /**
     * Session status: PENDING, APPROVED, EXPIRED, USED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TvAuthSessionStatus status = TvAuthSessionStatus.PENDING;

    /**
     * User who approved the session (set after approval).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private UserEntity approvedByUser;

    /**
     * Organization the TV will be linked to (set after approval).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    /**
     * Access token generated after approval (TV retrieves this).
     */
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    /**
     * Refresh token generated after approval.
     */
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    /**
     * Token expiry time.
     */
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    /**
     * When the session was created.
     */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * When the session expires (typically 10 minutes after creation).
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * When the session was approved.
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * Check if session is expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if session can be approved.
     */
    public boolean canBeApproved() {
        return status == TvAuthSessionStatus.PENDING && !isExpired();
    }
}
