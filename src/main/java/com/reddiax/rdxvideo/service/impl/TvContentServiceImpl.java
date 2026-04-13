package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.TvContentResponse;
import com.reddiax.rdxvideo.model.entity.*;
import com.reddiax.rdxvideo.repository.ScheduleRepository;
import com.reddiax.rdxvideo.repository.TickerContentRepository;
import com.reddiax.rdxvideo.repository.RssContentRepository;
import com.reddiax.rdxvideo.repository.WebpageContentRepository;
import com.reddiax.rdxvideo.service.TvContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.time.Duration;
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

    private static final Duration MEDIA_URL_TTL = Duration.ofHours(4);
    
    private final ScheduleRepository scheduleRepository;
    private final TickerContentRepository tickerContentRepository;
    private final RssContentRepository rssContentRepository;
    private final WebpageContentRepository webpageContentRepository;
    private final S3Presigner presigner;
    
    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Override
    @Transactional(readOnly = true)
    public TvContentResponse getTvContent(Long organizationId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        log.info("Looking for active schedule for org {} at date={}, time={}", organizationId, today, now);

        // First try to find time-specific active schedule
        List<ScheduleEntity> activeSchedules = scheduleRepository.findActiveSchedulesForDateTime(today, now);
        log.info("Found {} time-specific active schedules", activeSchedules.size());

        // Filter by organization
        ScheduleEntity schedule = activeSchedules.stream()
                .filter(s -> s.getOrganization() != null && s.getOrganization().getId().equals(organizationId))
                .findFirst()
                .orElse(null);

        // Fallback: if no time-specific schedule, get any active schedule for this org
        if (schedule == null) {
            log.info("No time-specific schedule, trying fallback for org {}", organizationId);
            List<ScheduleEntity> orgSchedules = scheduleRepository.findByOrganizationIdOrderByPriorityDescCreatedAtDesc(organizationId);
            log.info("Found {} total schedules for org {}", orgSchedules.size(), organizationId);
            
            for (ScheduleEntity s : orgSchedules) {
                log.info("Schedule: id={}, name={}, active={}, playlist={}", 
                        s.getId(), s.getName(), s.getActive(), 
                        s.getPlaylist() != null ? s.getPlaylist().getName() : "NULL");
            }
            
            schedule = orgSchedules.stream()
                    .filter(s -> Boolean.TRUE.equals(s.getActive()))
                    .findFirst()
                    .orElse(null);
            
            if (schedule != null) {
                log.info("Found fallback schedule: {} (id={})", schedule.getName(), schedule.getId());
            }
        }

        if (schedule == null) {
            log.info("No active schedule found for org {}", organizationId);
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
                    .url(presignUrl(media.getObjectKey()))
                    .thumbnailUrl(media.getThumbnailObjectKey() != null ? presignUrl(media.getThumbnailObjectKey()) : null)
                    .mediaType(media.getContentType() != null ? media.getContentType().name() : null);
        }

        // Fetch content details from respective tables
        switch (item.getContentType()) {
            case TICKER:
                if (item.getContentId() != null) {
                    tickerContentRepository.findById(item.getContentId()).ifPresent(ticker -> {
                        builder.name(ticker.getName())
                                .tickerText(ticker.getText())
                                .tickerSpeed(String.valueOf(ticker.getSpeed()))
                                .tickerDirection(ticker.getDirection())
                                .tickerBackgroundColor(ticker.getBackgroundColor())
                                .tickerTextColor(ticker.getTextColor())
                                .tickerFontFamily(ticker.getFontFamily())
                                .tickerFontSize(ticker.getFontSize());
                    });
                }
                break;
            case RSS:
                if (item.getContentId() != null) {
                    rssContentRepository.findById(item.getContentId()).ifPresent(rss -> {
                        builder.name(rss.getName())
                                .rssUrl(rss.getFeedUrl())
                                .rssRefreshInterval(rss.getRefreshIntervalMinutes())
                                .rssMaxItems(rss.getMaxItems())
                                .rssShowImages(rss.getShowImages())
                                .rssShowDescription(rss.getShowDescription())
                                .rssDisplayMode(rss.getDisplayMode())
                                .rssBackgroundColor(rss.getBackgroundColor())
                                .rssTextColor(rss.getTextColor());
                    });
                }
                break;
            case WEBPAGE:
                if (item.getContentId() != null) {
                    webpageContentRepository.findById(item.getContentId()).ifPresent(webpage -> {
                        builder.name(webpage.getName())
                                .webpageUrl(webpage.getUrl())
                                .webpageRefreshInterval(webpage.getRefreshIntervalSeconds())
                                .webpageScrollEnabled(webpage.getScrollEnabled())
                                .webpageZoomLevel(webpage.getZoomLevel());
                    });
                }
                break;
            default:
                break;
        }

        return builder.build();
    }
    
    private String presignUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(MEDIA_URL_TTL)
                    .getObjectRequest(getRequest)
                    .build();
            
            return presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("Failed to presign URL for key: {}", objectKey, e);
            return null;
        }
    }
}
