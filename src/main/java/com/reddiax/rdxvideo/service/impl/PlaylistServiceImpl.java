package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.constant.ContentStatusEnum;
import com.reddiax.rdxvideo.constant.ContentTypeEnum;
import com.reddiax.rdxvideo.model.dto.LayoutDTO;
import com.reddiax.rdxvideo.model.dto.LayoutSectionDTO;
import com.reddiax.rdxvideo.model.dto.PlaylistCreateRequest;
import com.reddiax.rdxvideo.model.dto.PlaylistDTO;
import com.reddiax.rdxvideo.model.dto.PlaylistItemDTO;
import com.reddiax.rdxvideo.model.dto.PlaylistItemRequest;
import com.reddiax.rdxvideo.model.entity.LayoutEntity;
import com.reddiax.rdxvideo.model.entity.MediaUploadEntity;
import com.reddiax.rdxvideo.model.entity.PlaylistEntity;
import com.reddiax.rdxvideo.model.entity.PlaylistItemEntity;
import com.reddiax.rdxvideo.repository.LayoutRepository;
import com.reddiax.rdxvideo.repository.MediaUploadRepository;
import com.reddiax.rdxvideo.repository.PlaylistItemRepository;
import com.reddiax.rdxvideo.repository.PlaylistRepository;
import com.reddiax.rdxvideo.repository.RssContentRepository;
import com.reddiax.rdxvideo.repository.ScheduleRepository;
import com.reddiax.rdxvideo.repository.TickerContentRepository;
import com.reddiax.rdxvideo.repository.WebpageContentRepository;
import com.reddiax.rdxvideo.service.PlaylistService;
import com.reddiax.rdxvideo.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistItemRepository playlistItemRepository;
    private final MediaUploadRepository mediaUploadRepository;
    private final ScheduleRepository scheduleRepository;
    private final LayoutRepository layoutRepository;
    private final TickerContentRepository tickerContentRepository;
    private final RssContentRepository rssContentRepository;
    private final WebpageContentRepository webpageContentRepository;
    private final S3Presigner presigner;
    private final PushNotificationService pushNotificationService;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    private static final Duration MEDIA_URL_TTL = Duration.ofMinutes(60);

    @Override
    public List<PlaylistDTO> getAllPlaylists() {
        return playlistRepository.findAllWithLayoutOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistDTO getPlaylist(Long id) {
        PlaylistEntity entity = playlistRepository.findByIdWithLayout(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
        
        log.info("Getting playlist {}, layout: {}, items: {}", id, 
                entity.getLayout() != null ? entity.getLayout().getId() : "null",
                entity.getItems().size());
        return toDTO(entity);
    }

    @Override
    @Transactional
    public PlaylistDTO createPlaylist(PlaylistCreateRequest request) {
        log.info("Creating playlist with layoutId: {}", request.getLayoutId());
        
        PlaylistEntity playlist = PlaylistEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .status(request.getStatus() != null ? request.getStatus() : ContentStatusEnum.DRAFT)
                .items(new ArrayList<>())
                .build();

        if (request.getLayoutId() != null) {
            LayoutEntity layout = layoutRepository.findById(request.getLayoutId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Layout not found: " + request.getLayoutId()));
            playlist.setLayout(layout);
            log.info("Layout set: {} ({})", layout.getName(), layout.getId());
        }

        playlist = playlistRepository.save(playlist);
        log.info("Playlist saved with id: {}, layout_id in entity: {}", 
                playlist.getId(), 
                playlist.getLayout() != null ? playlist.getLayout().getId() : "null");

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            addItemsToPlaylist(playlist, request.getItems());
        }

        return toDTO(playlist);
    }

    @Override
    @Transactional
    public PlaylistDTO updatePlaylist(Long id, PlaylistCreateRequest request) {
        PlaylistEntity playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            playlist.setStatus(request.getStatus());
        }

        if (request.getLayoutId() != null) {
            LayoutEntity layout = layoutRepository.findById(request.getLayoutId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Layout not found: " + request.getLayoutId()));
            playlist.setLayout(layout);
        } else {
            playlist.setLayout(null);
        }

        playlist.getItems().clear();
        
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            addItemsToPlaylist(playlist, request.getItems());
        }

        playlist = playlistRepository.save(playlist);
        
        // Notify all organizations that use this playlist
        notifyPlaylistChange(playlist.getId());
        
        return toDTO(playlist);
    }

    @Override
    @Transactional
    public void deletePlaylist(Long id) {
        if (!playlistRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found");
        }
        
        var linkedSchedules = scheduleRepository.findByPlaylistId(id);
        if (!linkedSchedules.isEmpty()) {
            String scheduleNames = linkedSchedules.stream()
                    .map(s -> s.getName())
                    .limit(3)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            
            String message = linkedSchedules.size() == 1 
                    ? "Playlista se koristi u rasporedu: " + scheduleNames
                    : "Playlista se koristi u " + linkedSchedules.size() + " rasporeda: " + scheduleNames;
            
            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }
        
        playlistRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PlaylistDTO toggleActive(Long id) {
        PlaylistEntity playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
        playlist.setActive(!playlist.getActive());
        playlist = playlistRepository.save(playlist);
        return toDTO(playlist);
    }

    @Override
    public List<PlaylistDTO> getPlaylistsByMediaId(Long mediaId) {
        return playlistRepository.findByMediaId(mediaId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public PlaylistDTO setPlaylistItems(Long playlistId, List<PlaylistItemRequest> items) {
        PlaylistEntity playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        // Clear existing items
        playlist.getItems().clear();
        playlistRepository.saveAndFlush(playlist);

        // Add new items
        if (items != null && !items.isEmpty()) {
            addItemsToPlaylist(playlist, items);
        }

        playlist.setModifiedAt(java.time.LocalDateTime.now());
        PlaylistEntity saved = playlistRepository.save(playlist);
        
        // Notify all organizations that use this playlist
        notifyPlaylistChange(saved.getId());
        
        return toDTO(saved);
    }

    private void notifyPlaylistChange(Long playlistId) {
        var linkedSchedules = scheduleRepository.findByPlaylistId(playlistId);
        linkedSchedules.stream()
                .filter(s -> s.getOrganization() != null)
                .map(s -> s.getOrganization().getId())
                .distinct()
                .forEach(orgId -> {
                    log.info("Sending content refresh push to organization {} for playlist change", orgId);
                    pushNotificationService.sendContentRefreshToOrganization(orgId);
                });
    }

    private void addItemsToPlaylist(PlaylistEntity playlist, List<PlaylistItemRequest> items) {
        for (PlaylistItemRequest itemRequest : items) {
            PlaylistItemEntity.PlaylistContentType contentType = 
                    PlaylistItemEntity.PlaylistContentType.MEDIA;
            
            if (itemRequest.getContentType() != null) {
                try {
                    contentType = PlaylistItemEntity.PlaylistContentType.valueOf(
                            itemRequest.getContentType().toUpperCase());
                } catch (IllegalArgumentException e) {
                    contentType = PlaylistItemEntity.PlaylistContentType.MEDIA;
                }
            }

            int duration = itemRequest.getDurationSeconds() != null 
                    ? itemRequest.getDurationSeconds() 
                    : 10;

            PlaylistItemEntity.PlaylistItemEntityBuilder builder = PlaylistItemEntity.builder()
                    .playlist(playlist)
                    .contentType(contentType)
                    .orderIndex(itemRequest.getOrderIndex())
                    .durationSeconds(duration)
                    .sectionId(itemRequest.getSectionId());

            if (contentType == PlaylistItemEntity.PlaylistContentType.MEDIA || 
                contentType == PlaylistItemEntity.PlaylistContentType.AUDIO) {
                Long mediaId = itemRequest.getMediaId() != null ? itemRequest.getMediaId() : itemRequest.getContentId();
                if (mediaId == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mediaId or contentId required for MEDIA/AUDIO type");
                }
                MediaUploadEntity media = mediaUploadRepository.findById(mediaId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                                "Media not found: " + mediaId));
                builder.media(media);
                builder.contentId(mediaId);
                
                if (itemRequest.getDurationSeconds() == null && 
                    (media.getContentType() == ContentTypeEnum.VIDEO || media.getContentType() == ContentTypeEnum.AUDIO) && 
                    media.getDurationInSeconds() != null) {
                    builder.durationSeconds(media.getDurationInSeconds().intValue());
                }
            } else {
                builder.contentId(itemRequest.getContentId());
            }

            playlist.getItems().add(builder.build());
        }
    }

    private PlaylistDTO toDTO(PlaylistEntity entity) {
        List<PlaylistItemDTO> items = entity.getItems().stream()
                .map(this::toItemDTO)
                .toList();

        int totalDuration = items.stream()
                .mapToInt(item -> item.getDurationSeconds() != null ? item.getDurationSeconds() : 0)
                .sum();

        LayoutDTO layoutDTO = null;
        Long layoutId = null;
        if (entity.getLayout() != null) {
            layoutId = entity.getLayout().getId();
            layoutDTO = toLayoutDTO(entity.getLayout());
        }

        return PlaylistDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .active(entity.getActive())
                .status(entity.getStatus())
                .layoutId(layoutId)
                .layout(layoutDTO)
                .items(items)
                .totalDurationSeconds(totalDuration)
                .itemCount(items.size())
                .createdAt(entity.getCreatedAt())
                .modifiedAt(entity.getModifiedAt())
                .build();
    }

    private LayoutDTO toLayoutDTO(LayoutEntity layout) {
        List<LayoutSectionDTO> sections = layout.getSections().stream()
                .map(section -> LayoutSectionDTO.builder()
                        .id(section.getSectionId())
                        .name(section.getName())
                        .x(section.getX())
                        .y(section.getY())
                        .width(section.getWidth())
                        .height(section.getHeight())
                        .contentType(section.getContentType().name().toLowerCase())
                        .color(section.getColor())
                        .zIndex(section.getZIndex())
                        .build())
                .toList();

        return LayoutDTO.builder()
                .id(layout.getId())
                .name(layout.getName())
                .description(layout.getDescription())
                .width(layout.getWidth())
                .height(layout.getHeight())
                .orientation(layout.getOrientation().name().toLowerCase())
                .sections(sections)
                .createdAt(layout.getCreatedAt())
                .updatedAt(layout.getModifiedAt())
                .build();
    }

    private PlaylistItemDTO toItemDTO(PlaylistItemEntity item) {
        PlaylistItemDTO.PlaylistItemDTOBuilder builder = PlaylistItemDTO.builder()
                .id(item.getId())
                .contentType(item.getContentType().name())
                .contentId(item.getContentId())
                .orderIndex(item.getOrderIndex())
                .durationSeconds(item.getDurationSeconds())
                .sectionId(item.getSectionId());

        if (item.getMedia() != null) {
            MediaUploadEntity media = item.getMedia();
            builder.mediaId(media.getId())
                   .mediaTitle(media.getOriginalFilename())
                   .mediaType(media.getContentType())
                   .thumbnailUrl(media.getThumbnailObjectKey() != null 
                           ? presignMedia(media.getThumbnailObjectKey()) 
                           : presignMedia(media.getObjectKey()))
                   .url(presignMedia(media.getObjectKey()))
                   .mediaDurationSeconds(media.getDurationInSeconds())
                   .name(media.getOriginalFilename());
        } else {
            builder.name(getContentName(item.getContentType(), item.getContentId()));
        }

        return builder.build();
    }

    private String getContentName(PlaylistItemEntity.PlaylistContentType type, Long contentId) {
        if (contentId == null) return "Unknown";
        
        return switch (type) {
            case TICKER -> tickerContentRepository.findById(contentId)
                    .map(t -> t.getName())
                    .orElse("Ticker #" + contentId);
            case RSS -> rssContentRepository.findById(contentId)
                    .map(r -> r.getName())
                    .orElse("RSS #" + contentId);
            case WEBPAGE -> webpageContentRepository.findById(contentId)
                    .map(w -> w.getName())
                    .orElse("Webpage #" + contentId);
            default -> "Content #" + contentId;
        };
    }

    private String presignMedia(String objectKey) {
        return presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(MEDIA_URL_TTL)
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(objectKey)
                                .build())
                        .build())
                .url()
                .toString();
    }
}
