package com.reddiax.rdxvideo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ALERT_CONTENT")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AlertContentEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private String severity = "WARNING";

    @Column(nullable = false)
    @Builder.Default
    private String displayMode = "BANNER";

    private String backgroundColor;

    private String textColor;

    private String iconName;

    @Column(nullable = false)
    @Builder.Default
    private Boolean showIcon = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoScroll = false;

    private Integer scrollSpeed;

    @Column(nullable = false)
    @Builder.Default
    private Boolean soundEnabled = false;

    private String soundUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean blinkEnabled = false;

    private LocalDateTime activeFrom;

    private LocalDateTime activeUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @ElementCollection
    @CollectionTable(name = "alert_content_tags", joinColumns = @JoinColumn(name = "alert_content_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();
}
