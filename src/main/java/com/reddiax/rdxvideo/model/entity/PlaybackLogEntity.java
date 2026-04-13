package com.reddiax.rdxvideo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "playback_log", indexes = {
    @Index(name = "idx_playback_log_device", columnList = "device_id"),
    @Index(name = "idx_playback_log_org", columnList = "organization_id"),
    @Index(name = "idx_playback_log_content", columnList = "content_id"),
    @Index(name = "idx_playback_log_started", columnList = "started_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaybackLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private TvDeviceRegistrationEntity device;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "duration_ms", nullable = false)
    private Integer durationMs;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;
}
