package com.reddiax.rdxvideo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "LAYOUT")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LayoutEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LayoutOrientation orientation;

    @OneToMany(mappedBy = "layout", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("zIndex ASC")
    @Builder.Default
    private List<LayoutSectionEntity> sections = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    public enum LayoutOrientation {
        LANDSCAPE, PORTRAIT
    }
}
