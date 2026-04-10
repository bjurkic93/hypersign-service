package com.reddiax.rdxvideo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "WEBPAGE_CONTENT")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WebpageContentEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    @Builder.Default
    private Integer refreshIntervalSeconds = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean scrollEnabled = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer zoomLevel = 100;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @ElementCollection
    @CollectionTable(name = "webpage_content_tags", joinColumns = @JoinColumn(name = "webpage_content_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();
}
