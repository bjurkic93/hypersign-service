package com.reddiax.rdxvideo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "PLAYLIST_ITEM")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PlaylistItemEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private PlaylistEntity playlist;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "media_id")
    private MediaUploadEntity media;

    @Column(name = "content_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PlaylistContentType contentType = PlaylistContentType.MEDIA;

    @Column(name = "content_id")
    private Long contentId;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    @Builder.Default
    private Integer durationSeconds = 10;

    @Column(name = "section_id")
    private String sectionId;

    public enum PlaylistContentType {
        MEDIA, AUDIO, TICKER, RSS, WEBPAGE
    }
}
