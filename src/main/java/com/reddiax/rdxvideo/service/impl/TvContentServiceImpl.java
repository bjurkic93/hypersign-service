package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.TvContentResponse;
import com.reddiax.rdxvideo.model.entity.*;
import com.reddiax.rdxvideo.repository.ScheduleRepository;
import com.reddiax.rdxvideo.service.TvContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TvContentServiceImpl implements TvContentService {

    private final ScheduleRepository scheduleRepository;

    @Override
    @Transactional(readOnly = true)
    public TvContentResponse getTvContent(Long organizationId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<ScheduleEntity> activeSchedules = scheduleRepository.findActiveSchedulesForDateTime(today, now);

        // Filter by organization
        ScheduleEntity schedule = activeSchedules.stream()
                .filter(s -> s.getOrganization() != null && s.getOrganization().getId().equals(organizationId))
                .findFirst()
                .orElse(null);

        if (schedule == null) {
            log.info("No active schedule found for org {} at {}", organizationId, now);
            return TvContentResponse.builder()
                    .schedule(null)
                    .playlist(null)
                    .layout(null)
                    .items(new ArrayList<>())
                    .generatedAt(LocalDateTime.now())
                    .build();
        }

        PlaylistEntity playlist = schedule.getPlaylist();
        if (playlist == null) {
            throw new RdXException(HttpStatus.NOT_FOUND, "Schedule has no playlist", "NO_PLAYLIST");
        }

        LayoutEntity layout = playlist.getLayout();

        // Build response
        TvContentResponse.ScheduleInfo scheduleInfo = TvContentResponse.ScheduleInfo.builder()
                .id(schedule.getId())
                .name(schedule.getName())
                .priority(schedule.getPriority())
                .active(schedule.getActive())
                .build();

        TvContentResponse.PlaylistInfo playlistInfo = TvContentResponse.PlaylistInfo.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .totalDurationSeconds(calculateTotalDuration(playlist.getItems()))
                .itemCount(playlist.getItems().size())
                .build();

        TvContentResponse.LayoutInfo layoutInfo = null;
        if (layout != null) {
            layoutInfo = TvContentResponse.LayoutInfo.builder()
                    .id(layout.getId())
                    .name(layout.getName())
                    .width(layout.getWidth())
                    .height(layout.getHeight())
                    .orientation(layout.getOrientation().name())
                    .sections(mapSections(layout.getSections()))
                    .build();
        }

        List<TvContentResponse.ContentItem> items = mapPlaylistItems(playlist.getItems());

        log.info("Returning TV content for org {}: schedule={}, playlist={}, items={}",
                organizationId, schedule.getName(), playlist.getName(), items.size());

        return TvContentResponse.builder()
                .schedule(scheduleInfo)
                .playlist(playlistInfo)
                .layout(layoutInfo)
                .items(items)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private Integer calculateTotalDuration(List<PlaylistItemEntity> items) {
        return items.stream()
                .mapToInt(PlaylistItemEntity::getDurationSeconds)
                .sum();
    }

    private List<TvContentResponse.SectionInfo> mapSections(List<LayoutSectionEntity> sections) {
        return sections.stream()
                .map(s -> TvContentResponse.SectionInfo.builder()
                        .id(s.getSectionId())
                        .name(s.getName())
                        .type(s.getContentType().name())
                        .x(s.getX().doubleValue())
                        .y(s.getY().doubleValue())
                        .width(s.getWidth().doubleValue())
                        .height(s.getHeight().doubleValue())
                        .zIndex(s.getZIndex())
                        .backgroundColor(s.getColor())
                        .build())
                .collect(Collectors.toList());
    }

    private List<TvContentResponse.ContentItem> mapPlaylistItems(List<PlaylistItemEntity> items) {
        return items.stream()
                .map(this::mapContentItem)
                .collect(Collectors.toList());
    }

    private TvContentResponse.ContentItem mapContentItem(PlaylistItemEntity item) {
        TvContentResponse.ContentItem.ContentItemBuilder builder = TvContentResponse.ContentItem.builder()
                .id(item.getId())
                .sectionId(item.getSectionId())
                .contentType(item.getContentType().name())
                .contentId(item.getContentId())
                .durationSeconds(item.getDurationSeconds())
                .orderIndex(item.getOrderIndex());

        if (item.getMedia() != null) {
            MediaUploadEntity media = item.getMedia();
            builder.name(media.getOriginalFilename())
                    .url(media.getObjectKey())
                    .thumbnailUrl(media.getThumbnailObjectKey())
                    .mediaType(media.getContentType() != null ? media.getContentType().name() : null);
        }

        // For non-media content types, we'd need to fetch from respective tables
        // For now, set contentId which can be used to fetch details
        switch (item.getContentType()) {
            case TICKER:
                // Could fetch TickerEntity if exists
                builder.name("Ticker #" + item.getContentId());
                break;
            case RSS:
                // Could fetch RssEntity if exists
                builder.name("RSS Feed #" + item.getContentId());
                break;
            case WEBPAGE:
                // Could fetch WebpageEntity if exists
                builder.name("Webpage #" + item.getContentId());
                break;
            default:
                break;
        }

        return builder.build();
    }
}
