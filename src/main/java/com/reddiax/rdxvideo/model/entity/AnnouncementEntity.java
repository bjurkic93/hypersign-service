package com.reddiax.rdxvideo.model.entity;

import com.reddiax.rdxvideo.constant.AnnouncementChannelEnum;
import com.reddiax.rdxvideo.constant.AnnouncementStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for storing announcements that can be sent to all users
 * via push notifications and/or email.
 */
@Entity
@Table(name = "announcement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(columnDefinition = "TEXT")
    private String htmlContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnouncementChannelEnum channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnouncementStatusEnum status;

    @Column
    private LocalDateTime scheduledAt;

    @Column
    private LocalDateTime sentAt;

    @Column
    private Integer totalRecipients;

    @Column
    private Integer successfulDeliveries;

    @Column
    private Integer failedDeliveries;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private UserEntity createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private boolean active = true;
}
