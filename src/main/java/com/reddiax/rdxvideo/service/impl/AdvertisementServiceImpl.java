package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.constant.AdvertisementMediaTypeEnum;
import com.reddiax.rdxvideo.constant.AdvertisementPlatformEnum;
import com.reddiax.rdxvideo.constant.OrganizationTypeEnum;
import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.*;
import com.reddiax.rdxvideo.model.entity.AdvertisementEntity;
import com.reddiax.rdxvideo.model.entity.AdvertisementImageEntity;
import com.reddiax.rdxvideo.model.entity.AdvertisementVideoEntity;
import com.reddiax.rdxvideo.model.entity.OrganizationEntity;
import com.reddiax.rdxvideo.model.mapper.AdvertisementImageMapper;
import com.reddiax.rdxvideo.model.mapper.AdvertisementMapper;
import com.reddiax.rdxvideo.model.mapper.PricingTierMapper;
import com.reddiax.rdxvideo.repository.*;
import com.reddiax.rdxvideo.service.AdvertisementService;
import com.reddiax.rdxvideo.service.ImageHashService;
import com.reddiax.rdxvideo.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementImageRepository advertisementImageRepository;
    private final AdvertisementVideoRepository advertisementVideoRepository;
    private final OrganizationRepository organizationRepository;
    private final PricingTierRepository pricingTierRepository;
    private final MediaUploadRepository mediaUploadRepository;
    private final UserAdvertisementRepository userAdvertisementRepository;
    private final MediaService mediaService;
    private final ImageHashService imageHashService;

    @Value("${advertisement.max-duration-seconds:300}")
    private int maxDurationSeconds;

    @Override
    @Transactional(readOnly = true)
    public Page<AdvertisementDTO> getAdvertisements(Pageable pageable) {
        return advertisementRepository.findAll(pageable)
                .map(this::toAdvertisementDtoWithDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdvertisementDTO> getAdvertisementsByOrganization(Long organizationId, Pageable pageable) {
        return advertisementRepository.findByOrganizationId(organizationId, pageable)
                .map(this::toAdvertisementDtoWithDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdvertisementDTO> getAdvertisementsByPlatform(AdvertisementPlatformEnum platform, Pageable pageable) {
        return advertisementRepository.findByPlatform(platform, pageable)
                .map(this::toAdvertisementDtoWithDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdvertisementDTO> getValidAdvertisements(AdvertisementPlatformEnum platform, Long organizationId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return advertisementRepository.findValidAdvertisementsByPlatformAndOrganization(platform, organizationId, now, pageable)
                .map(this::toAdvertisementDtoWithDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public AdvertisementDTO getAdvertisement(Long id) {
        return toAdvertisementDtoWithDetails(findAdvertisementById(id));
    }

    @Override
    @Transactional
    public AdvertisementDTO createAdvertisement(AdvertisementRequestDTO request) {
        validateAdvertisementRequest(request);

        AdvertisementEntity entity = AdvertisementMapper.INSTANCE.toCreateEntity(request);

        if (entity.getIsPublic() == null) {
            entity.setIsPublic(false);
        }
        
        if (entity.getActive() == null) {
            entity.setActive(true);
        }

        AdvertisementEntity savedEntity = advertisementRepository.save(entity);

        // Save advertisement videos for VIDEO_SET or MIXED_PLAYLIST
        if ((request.getMediaType() == AdvertisementMediaTypeEnum.VIDEO_SET || 
             request.getMediaType() == AdvertisementMediaTypeEnum.MIXED_PLAYLIST) && 
            request.getVideos() != null) {
            saveAdvertisementVideos(savedEntity.getId(), request.getVideos());
        }

        // Save advertisement images for IMAGE_SET or MIXED_PLAYLIST
        if ((request.getMediaType() == AdvertisementMediaTypeEnum.IMAGE_SET || 
             request.getMediaType() == AdvertisementMediaTypeEnum.MIXED_PLAYLIST) && 
            request.getImages() != null) {
            saveAdvertisementImages(savedEntity.getId(), request.getImages());
        }

        return toAdvertisementDtoWithDetails(savedEntity);
    }

    @Override
    @Transactional
    public AdvertisementDTO updateAdvertisement(Long id, AdvertisementRequestDTO request) {
        AdvertisementEntity existingEntity = findAdvertisementById(id);

        validateAdvertisementRequest(request);

        AdvertisementMapper.INSTANCE.updateEntity(request, existingEntity);

        AdvertisementEntity savedEntity = advertisementRepository.save(existingEntity);

        // Update advertisement videos
        advertisementVideoRepository.deleteByAdvertisementId(id);
        if ((request.getMediaType() == AdvertisementMediaTypeEnum.VIDEO_SET || 
             request.getMediaType() == AdvertisementMediaTypeEnum.MIXED_PLAYLIST) && 
            request.getVideos() != null) {
            saveAdvertisementVideos(id, request.getVideos());
        }

        // Update advertisement images
        advertisementImageRepository.deleteByAdvertisementId(id);
        if ((request.getMediaType() == AdvertisementMediaTypeEnum.IMAGE_SET || 
             request.getMediaType() == AdvertisementMediaTypeEnum.MIXED_PLAYLIST) && 
            request.getImages() != null) {
            saveAdvertisementImages(id, request.getImages());
        }

        return toAdvertisementDtoWithDetails(savedEntity);
    }

    @Override
    @Transactional
    public void deleteAdvertisement(Long id) {
        AdvertisementEntity advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND,
                        "Advertisement not found with id: " + id, "ADVERTISEMENT_NOT_FOUND"));
        
        // Check if there are any active promotions
        if (userAdvertisementRepository.existsActiveByAdvertisementId(id)) {
            throw new RdXException(HttpStatus.BAD_REQUEST,
                    "Cannot delete advertisement with active promotions", "ACTIVE_PROMOTIONS_EXIST");
        }
        
        // Soft delete - just mark as inactive
        advertisement.setActive(false);
        advertisementRepository.save(advertisement);
        
        log.info("Advertisement {} marked as deleted (soft delete)", id);
    }

    private void validateAdvertisementRequest(AdvertisementRequestDTO request) {
        // Validate date range
        if (request.getValidFrom() != null && request.getValidTo() != null) {
            if (request.getValidFrom().isAfter(request.getValidTo())) {
                throw new RdXException(HttpStatus.BAD_REQUEST,
                        "Valid from date must be before valid to date", "INVALID_DATE_RANGE");
            }
        }

        // Validate organization exists and is of type ADVERTISER
        if (request.getOrganizationId() != null) {
            OrganizationEntity org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new RdXException(HttpStatus.BAD_REQUEST,
                            "Organization not found with id: " + request.getOrganizationId(), "ORGANIZATION_NOT_FOUND"));

            if (org.getOrganizationType() != OrganizationTypeEnum.ADVERTISER) {
                throw new RdXException(HttpStatus.BAD_REQUEST,
                        "Organization must be of type ADVERTISER", "INVALID_ORGANIZATION_TYPE");
            }
        }

        // Validate media type specific requirements
        if (request.getMediaType() == AdvertisementMediaTypeEnum.VIDEO) {
            // Legacy single video type
            if (request.getVideoId() == null) {
                throw new RdXException(HttpStatus.BAD_REQUEST,
                        "Video ID is required for VIDEO media type", "VIDEO_ID_REQUIRED");
            }
            if (!mediaUploadRepository.existsById(request.getVideoId())) {
                throw new RdXException(HttpStatus.BAD_REQUEST,
                        "Video not found with id: " + request.getVideoId(), "VIDEO_NOT_FOUND");
            }
        } else if (request.getMediaType() == AdvertisementMediaTypeEnum.VIDEO_SET) {
            // Multiple videos only
            if (request.getVideos() == null || request.getVideos().isEmpty()) {
                throw new RdXException(HttpStatus.BAD_REQUEST,
                        "At least one video is required for VIDEO_SET media type", "VIDEOS_REQUIRED");
            }
            validateVideos(request.getVideos(), request.getPlatform());
        } else if (request.getMediaType() == AdvertisementMediaTypeEnum.IMAGE_SET) {
            // Multiple images only
            if (request.getImages() == null || request.getImages().isEmpty()) {
                throw new RdXException(HttpStatus.BAD_REQUEST,
                        "At least one image is required for IMAGE_SET media type", "IMAGES_REQUIRED");
            }
            validateImages(request.getImages(), request.getPlatform());
        } else if (request.getMediaType() == AdvertisementMediaTypeEnum.MIXED_PLAYLIST) {
            // Combination of videos and images
            boolean hasVideos = request.getVideos() != null && !request.getVideos().isEmpty();
            boolean hasImages = request.getImages() != null && !request.getImages().isEmpty();
            
            if (!hasVideos && !hasImages) {
                throw new RdXException(HttpStatus.BAD_REQUEST,
                        "At least one video or image is required for MIXED_PLAYLIST media type", "MEDIA_REQUIRED");
            }
            
            if (hasVideos) {
                validateVideos(request.getVideos(), request.getPlatform());
            }
            if (hasImages) {
                validateImages(request.getImages(), request.getPlatform());
            }
            
            // Log combined duration for TV platform (no max limit for playlists)
            if (request.getPlatform() == AdvertisementPlatformEnum.TV) {
                int totalDuration = 0;
                if (hasVideos) {
                    totalDuration += request.getVideos().stream()
                            .filter(v -> v.getDisplayDurationSeconds() != null)
                            .mapToInt(AdvertisementVideoRequestDTO::getDisplayDurationSeconds)
                            .sum();
                }
                if (hasImages) {
                    totalDuration += request.getImages().stream()
                            .filter(img -> img.getDisplayDurationSeconds() != null)
                            .mapToInt(AdvertisementImageRequestDTO::getDisplayDurationSeconds)
                            .sum();
                }
                log.debug("Mixed playlist total duration: {} seconds ({} videos, {} images)", 
                        totalDuration, 
                        hasVideos ? request.getVideos().size() : 0, 
                        hasImages ? request.getImages().size() : 0);
            }
        }

        // Validate platform specific requirements (TV only)
        if (request.getPricingTierId() == null) {
            throw new RdXException(HttpStatus.BAD_REQUEST,
                    "Pricing tier is required for TV platform", "PRICING_TIER_REQUIRED");
        }
        if (!pricingTierRepository.existsById(request.getPricingTierId())) {
            throw new RdXException(HttpStatus.BAD_REQUEST,
                    "Pricing tier not found with id: " + request.getPricingTierId(), "PRICING_TIER_NOT_FOUND");
        }
    }

    private void saveAdvertisementImages(Long advertisementId, List<AdvertisementImageRequestDTO> images) {
        for (AdvertisementImageRequestDTO imageRequest : images) {
            AdvertisementImageEntity imageEntity = AdvertisementImageMapper.INSTANCE.toEntity(imageRequest);
            imageEntity.setAdvertisementId(advertisementId);
            
            // Calculate image hash for wallpaper verification
            String imageHash = imageHashService.calculateImageHash(imageRequest.getImageId());
            if (imageHash != null) {
                imageEntity.setImageHash(imageHash);
                log.debug("Calculated hash for image {}: {}", imageRequest.getImageId(), imageHash);
            } else {
                log.warn("Failed to calculate hash for image {}", imageRequest.getImageId());
            }
            
            advertisementImageRepository.save(imageEntity);
        }
    }

    private void saveAdvertisementVideos(Long advertisementId, List<AdvertisementVideoRequestDTO> videos) {
        for (AdvertisementVideoRequestDTO videoRequest : videos) {
            AdvertisementVideoEntity videoEntity = AdvertisementVideoEntity.builder()
                    .advertisementId(advertisementId)
                    .videoId(videoRequest.getVideoId())
                    .displayOrder(videoRequest.getDisplayOrder())
                    .displayDurationSeconds(videoRequest.getDisplayDurationSeconds())
                    .title(videoRequest.getTitle())
                    .build();
            
            advertisementVideoRepository.save(videoEntity);
            log.debug("Saved video {} for advertisement {}", videoRequest.getVideoId(), advertisementId);
        }
    }

    private void validateVideos(List<AdvertisementVideoRequestDTO> videos, AdvertisementPlatformEnum platform) {
        for (AdvertisementVideoRequestDTO video : videos) {
            if (!mediaUploadRepository.existsById(video.getVideoId())) {
                throw new RdXException(HttpStatus.BAD_REQUEST,
                        "Video not found with id: " + video.getVideoId(), "VIDEO_NOT_FOUND");
            }
        }
        
        // TV platform: validate duration if specified
        if (platform == AdvertisementPlatformEnum.TV) {
            // Duration is optional for videos - if not set, full video plays
            // Could add max total duration validation here if needed
        }
    }

    private void validateImages(List<AdvertisementImageRequestDTO> images, AdvertisementPlatformEnum platform) {
        // Validate duration based on platform
        if (platform == AdvertisementPlatformEnum.TV) {
            // TV platform: validate total duration in seconds
            int totalDuration = images.stream()
                    .filter(img -> img.getDisplayDurationSeconds() != null)
                    .mapToInt(AdvertisementImageRequestDTO::getDisplayDurationSeconds)
                    .sum();
            
            if (totalDuration > maxDurationSeconds) {
                throw new RdXException(HttpStatus.BAD_REQUEST,
                        "Total image duration (" + totalDuration + "s) exceeds maximum allowed (" + maxDurationSeconds + "s)", 
                        "MAX_DURATION_EXCEEDED");
            }
        }
    }

    private AdvertisementEntity findAdvertisementById(Long id) {
        return advertisementRepository.findById(id)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND,
                        "Advertisement not found with id: " + id, "ADVERTISEMENT_NOT_FOUND"));
    }

    private AdvertisementDTO toAdvertisementDtoWithDetails(AdvertisementEntity entity) {
        AdvertisementDTO dto = AdvertisementMapper.INSTANCE.toDto(entity);

        // Resolve organization name
        if (entity.getOrganizationId() != null) {
            organizationRepository.findById(entity.getOrganizationId())
                    .ifPresent(org -> dto.setOrganizationName(org.getName()));
        }

        // Resolve video URL
        if (entity.getVideoId() != null) {
            try {
                MediaUrlResponseDTO videoUrl = mediaService.getMediaUrl(entity.getVideoId());
                dto.setVideoUrl(videoUrl.getUrl());
            } catch (Exception e) {
                log.warn("Failed to resolve video URL for advertisement {}: {}", entity.getId(), e.getMessage());
            }
        }

        // Resolve videos for VIDEO_SET or MIXED_PLAYLIST
        if (entity.getMediaType() == AdvertisementMediaTypeEnum.VIDEO_SET || 
            entity.getMediaType() == AdvertisementMediaTypeEnum.MIXED_PLAYLIST) {
            List<AdvertisementVideoEntity> videoEntities = 
                    advertisementVideoRepository.findByAdvertisementIdOrderByDisplayOrderAsc(entity.getId());
            
            List<AdvertisementVideoDTO> videoDtos = new ArrayList<>();
            for (AdvertisementVideoEntity videoEntity : videoEntities) {
                AdvertisementVideoDTO videoDto = AdvertisementVideoDTO.builder()
                        .id(videoEntity.getId())
                        .advertisementId(videoEntity.getAdvertisementId())
                        .videoId(videoEntity.getVideoId())
                        .displayOrder(videoEntity.getDisplayOrder())
                        .displayDurationSeconds(videoEntity.getDisplayDurationSeconds())
                        .title(videoEntity.getTitle())
                        .build();
                try {
                    MediaUrlResponseDTO videoUrl = mediaService.getMediaUrl(videoEntity.getVideoId());
                    videoDto.setVideoUrl(videoUrl.getUrl());
                } catch (Exception e) {
                    log.warn("Failed to resolve video URL for advertisement video {}: {}", 
                            videoEntity.getId(), e.getMessage());
                }
                videoDtos.add(videoDto);
            }
            dto.setVideos(videoDtos);
        }

        // Resolve images for IMAGE_SET or MIXED_PLAYLIST
        if (entity.getMediaType() == AdvertisementMediaTypeEnum.IMAGE_SET || 
            entity.getMediaType() == AdvertisementMediaTypeEnum.MIXED_PLAYLIST) {
            List<AdvertisementImageEntity> imageEntities = 
                    advertisementImageRepository.findByAdvertisementIdOrderByDisplayOrderAsc(entity.getId());
            
            List<AdvertisementImageDTO> imageDtos = new ArrayList<>();
            for (AdvertisementImageEntity imageEntity : imageEntities) {
                AdvertisementImageDTO imageDto = AdvertisementImageMapper.INSTANCE.toDto(imageEntity);
                try {
                    MediaUrlResponseDTO imageUrl = mediaService.getMediaUrl(imageEntity.getImageId());
                    imageDto.setImageUrl(imageUrl.getUrl());
                } catch (Exception e) {
                    log.warn("Failed to resolve image URL for advertisement image {}: {}", 
                            imageEntity.getId(), e.getMessage());
                }
                imageDtos.add(imageDto);
            }
            dto.setImages(imageDtos);
        }

        // Resolve pricing tier
        if (entity.getPricingTierId() != null) {
            pricingTierRepository.findById(entity.getPricingTierId())
                    .ifPresent(tier -> dto.setPricingTier(PricingTierMapper.INSTANCE.toDto(tier)));
        }

        return dto;
    }
}
