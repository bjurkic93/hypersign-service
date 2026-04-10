package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.ContentTypeEnum;
import com.reddiax.rdxvideo.constant.MediaUploadStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadCompleteResponse {
    private Long id;
    private ContentTypeEnum contentType;
    private String objectKey;
    private MediaUploadStatusEnum status;
    private String url;
}
