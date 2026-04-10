package com.reddiax.rdxvideo.model.entity;

import com.reddiax.rdxvideo.constant.ContentTypeEnum;
import com.reddiax.rdxvideo.constant.MediaUploadStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "MEDIA_UPLOAD")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class MediaUploadEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentTypeEnum contentType;

    private String objectKey;
    private String thumbnailObjectKey;
    private String originalFilename;
    private String mimeType;
    private String folder;

    private Long expectedSize;
    private Long uploadedSize;
    private String etag;

    private Integer width;
    private Integer height;
    private Long durationInSeconds;

    @Enumerated(EnumType.STRING)
    private MediaUploadStatusEnum status;

    private LocalDateTime expiresAt;
    private LocalDateTime uploadedAt;
    private LocalDateTime lastCheckAt;
    private String failReason;

    @ElementCollection
    @CollectionTable(name = "media_upload_tags", joinColumns = @JoinColumn(name = "media_upload_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();
}
