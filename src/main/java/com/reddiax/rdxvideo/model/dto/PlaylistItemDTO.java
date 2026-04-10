package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.ContentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistItemDTO {
    private Long id;
    private String contentType; // MEDIA, TICKER, RSS, WEBPAGE
    private Long contentId;
    private Long mediaId;
    private String mediaTitle;
    private ContentTypeEnum mediaType;
    private String thumbnailUrl;
    private String url;
    private Integer orderIndex;
    private Integer durationSeconds;
    private Long mediaDurationSeconds;
    private String sectionId;
    private String name; // Generic name for non-media items
}
