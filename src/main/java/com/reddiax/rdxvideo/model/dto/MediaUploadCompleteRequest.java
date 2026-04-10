package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadCompleteRequest {
    private String objectKey;
    private Integer width;
    private Integer height;
    private Long durationInSeconds;
    private String thumbnailObjectKey;
}
