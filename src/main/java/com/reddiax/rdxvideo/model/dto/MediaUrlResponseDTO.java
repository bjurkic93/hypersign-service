package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.ContentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUrlResponseDTO {
    private Long id;
    private ContentTypeEnum contentType;
    private String objectKey;
    private String url;
    private String thumbnailUrl;
    private LocalDateTime expiresAt;
}
