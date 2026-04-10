package com.reddiax.rdxvideo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "LAYOUT_SECTION")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LayoutSectionEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sectionId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer x;

    @Column(nullable = false)
    private Integer y;

    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Integer zIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layout_id", nullable = false)
    private LayoutEntity layout;

    public enum ContentType {
        IMAGE, VIDEO, AUDIO, RSS, WEB, APP, ANY
    }
}
