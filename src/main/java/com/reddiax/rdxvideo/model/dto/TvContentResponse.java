package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response containing all content needed for TV playback.
 * Includes active schedule, playlist with layout, and all content items.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TvContentResponse {

    private ScheduleInfo schedule;
    private PlaylistInfo playlist;
    private LayoutInfo layout;
    private List<ContentItem> items;
    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleInfo {
        private Long id;
        private String name;
        private Integer priority;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaylistInfo {
        private Long id;
        private String name;
        private String description;
        private Integer totalDurationSeconds;
        private Integer itemCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LayoutInfo {
        private Long id;
        private String name;
        private Integer width;
        private Integer height;
        private String orientation;
        private List<SectionInfo> sections;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionInfo {
        private String id;
        private String name;
        private String type;
        private Double x;
        private Double y;
        private Double width;
        private Double height;
        private Integer zIndex;
        private String backgroundColor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentItem {
        private Long id;
        private String sectionId;
        private String contentType;  // MEDIA, TICKER, RSS, WEBPAGE
        private Long contentId;
        private String name;
        private String url;
        private String thumbnailUrl;
        private String mediaType;    // VIDEO, IMAGE, AUDIO
        private Integer durationSeconds;
        private Integer orderIndex;
        
        // For TICKER type
        private String tickerText;
        private String tickerSpeed;
        private String tickerDirection;
        
        // For RSS type
        private String rssUrl;
        private Integer rssRefreshInterval;
        
        // For WEBPAGE type
        private String webpageUrl;
    }
}
