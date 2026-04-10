package com.reddiax.rdxvideo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "RSS_CONTENT")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class RssContentEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String feedUrl;

    @Column(nullable = false)
    @Builder.Default
    private Integer refreshIntervalMinutes = 15;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxItems = 10;

    @Column(nullable = false)
    @Builder.Default
    private Boolean showImages = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean showDescription = true;

    @Column(nullable = false)
    @Builder.Default
    private String displayMode = "LIST";

    private String backgroundColor;

    private String textColor;

    private String fontFamily;

    private Integer fontSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @ElementCollection
    @CollectionTable(name = "rss_content_tags", joinColumns = @JoinColumn(name = "rss_content_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();
}
