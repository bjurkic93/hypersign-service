package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.ContentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaListItemDTO {
    private Long id;
    private ContentTypeEnum contentType;
    private String objectKey;
    private String thumbnailObjectKey;
    private String thumbnailUrl;
    private String url;
    private String originalFilename;
    private String mimeType;
    private Long uploadedSize;
    private Integer width;
    private Integer height;
    private Long durationInSeconds;
    private LocalDateTime createdAt;
    private List<String> tags;
}
