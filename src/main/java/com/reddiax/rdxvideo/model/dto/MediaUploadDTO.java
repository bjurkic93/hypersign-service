package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.ContentTypeEnum;
import com.reddiax.rdxvideo.constant.MediaUploadStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadDTO {
    private Long id;
    private ContentTypeEnum contentType;
    private String objectKey;
    private String thumbnailObjectKey;
    private String originalFilename;
    private String mimeType;
    private String folder;
    private Long uploadedSize;
    private Integer width;
    private Integer height;
    private Long durationInSeconds;
    private MediaUploadStatusEnum status;
    private LocalDateTime createdAt;
    private LocalDateTime uploadedAt;
}
