package com.reddiax.rdxvideo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "TICKER_CONTENT")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TickerContentEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(nullable = false)
    @Builder.Default
    private Integer speed = 50;

    @Column(nullable = false)
    @Builder.Default
    private String direction = "LEFT";

    private String backgroundColor;

    private String textColor;

    private String fontFamily;

    private Integer fontSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @ElementCollection
    @CollectionTable(name = "ticker_content_tags", joinColumns = @JoinColumn(name = "ticker_content_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();
}
