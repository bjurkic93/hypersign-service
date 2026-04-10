package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.constant.ContentTypeEnum;
import com.reddiax.rdxvideo.constant.MediaUploadStatusEnum;
import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.*;
import com.reddiax.rdxvideo.model.entity.MediaUploadEntity;
import com.reddiax.rdxvideo.repository.MediaUploadRepository;
import com.reddiax.rdxvideo.repository.OrganizationRepository;
import com.reddiax.rdxvideo.repository.UserRepository;
import com.reddiax.rdxvideo.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final Duration THUMBNAIL_URL_TTL = Duration.ofMinutes(60);
    private static final Duration MEDIA_URL_TTL = Duration.ofMinutes(60);

    private final MediaUploadRepository mediaUploadRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final S3Presigner presigner;
    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.presign-minutes:15}")
    private Long presignMinutes;

    @Override
    public MediaUploadLinkResponse getUploadLink(ContentTypeEnum contentType, String filename, String mimeType, Long expectedSize, String folder) {
        String safeFolder = getSafeFolder(contentType, folder);
        String uploadId = UUID.randomUUID().toString();
        String objectKey = safeFolder + "/" + uploadId + getExtension(filename, mimeType);

        MediaUploadEntity entity = MediaUploadEntity.builder()
                .contentType(contentType)
                .objectKey(objectKey)
                .originalFilename(filename)
                .mimeType(mimeType)
                .folder(safeFolder)
                .expectedSize(expectedSize)
                .status(MediaUploadStatusEnum.PENDING)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMinutes(presignMinutes)))
                .build();

        entity = mediaUploadRepository.save(entity);

        PresignedPutObjectRequest presigned = presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(presignMinutes))
                        .putObjectRequest(PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(objectKey)
                                .contentType(mimeType)
                                .build())
                        .build());

        return MediaUploadLinkResponse.builder()
                .id(entity.getId())
                .contentType(contentType)
                .objectKey(objectKey)
                .uploadUrl(presigned.url().toString())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    @Transactional
    @Override
    public MediaUploadCompleteResponse uploadComplete(MediaUploadCompleteRequest request) {
        LocalDateTime now = LocalDateTime.now();
        MediaUploadEntity entity = mediaUploadRepository.findByObjectKey(request.getObjectKey())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Upload not found"));

        if (entity.getStatus() == MediaUploadStatusEnum.UPLOADED) {
            return MediaUploadCompleteResponse.builder()
                    .id(entity.getId())
                    .contentType(entity.getContentType())
                    .objectKey(entity.getObjectKey())
                    .status(MediaUploadStatusEnum.UPLOADED)
                    .url(presignMedia(entity.getObjectKey()))
                    .build();
        }

        HeadObjectResponse headResponse = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucket)
                .key(request.getObjectKey())
                .build());

        if (entity.getExpectedSize() != null && !headResponse.contentLength().equals(entity.getExpectedSize())) {
            entity.setStatus(MediaUploadStatusEnum.FAILED);
            entity.setFailReason("Size mismatch: expected=" + entity.getExpectedSize() + ", actual=" + headResponse.contentLength());
            entity.setLastCheckAt(now);
            mediaUploadRepository.save(entity);
            throw new RdXException(HttpStatus.CONFLICT, "Size mismatch", "UPLOAD_SIZE_MISMATCH");
        }

        entity.setStatus(MediaUploadStatusEnum.UPLOADED);
        entity.setUploadedSize(headResponse.contentLength());
        entity.setEtag(headResponse.eTag());
        entity.setWidth(request.getWidth());
        entity.setHeight(request.getHeight());
        entity.setDurationInSeconds(request.getDurationInSeconds());
        entity.setThumbnailObjectKey(request.getThumbnailObjectKey());
        entity.setUploadedAt(now);
        entity.setLastCheckAt(now);
        mediaUploadRepository.save(entity);

        return MediaUploadCompleteResponse.builder()
                .id(entity.getId())
                .contentType(entity.getContentType())
                .objectKey(entity.getObjectKey())
                .status(MediaUploadStatusEnum.UPLOADED)
                .url(presignMedia(entity.getObjectKey()))
                .build();
    }

    @Override
    public MediaPageResponseDTO listMedia(Pageable pageable, ContentTypeEnum contentType, String folder, String search, List<String> tags) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pagination parameters");
        }

        Sort defaultSort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        Pageable effectivePageable = pageable;
        if (pageable.isUnpaged()) {
            effectivePageable = PageRequest.of(0, 20, defaultSort);
        } else if (pageable.getSort().isUnsorted()) {
            effectivePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort);
        }

        final String folderPrefix = (folder != null && !folder.isBlank()) ? folder.trim() + "/" : null;
        final String searchPattern = (search != null && !search.isBlank()) ? "%" + search.trim().toLowerCase() + "%" : null;
        Set<Long> excludedImageIds = (folderPrefix == null && contentType == ContentTypeEnum.IMAGE) ? getExcludedImageIds() : Set.of();

        Specification<MediaUploadEntity> specification = (root, query, builder) -> {
            var statusPredicate = builder.equal(root.get("status"), MediaUploadStatusEnum.UPLOADED);
            var predicates = builder.and(statusPredicate);

            if (contentType != null) {
                predicates = builder.and(predicates, builder.equal(root.get("contentType"), contentType));
            }

            if (folderPrefix != null) {
                predicates = builder.and(predicates, builder.like(root.get("objectKey"), folderPrefix + "%"));
            }

            if (searchPattern != null) {
                predicates = builder.and(predicates, builder.like(builder.lower(root.get("originalFilename")), searchPattern));
            }

            if (!excludedImageIds.isEmpty()) {
                predicates = builder.and(predicates, builder.not(root.get("id").in(excludedImageIds)));
            }

            return predicates;
        };

        Page<MediaUploadEntity> result = mediaUploadRepository.findAll(specification, effectivePageable);
        
        // Filter by tags in memory if provided (JPA ElementCollection filtering is complex)
        List<MediaListItemDTO> items = result.getContent().stream()
                .filter(entity -> {
                    if (tags == null || tags.isEmpty()) return true;
                    return entity.getTags() != null && entity.getTags().stream().anyMatch(tags::contains);
                })
                .map(this::toListItem)
                .toList();

        return MediaPageResponseDTO.builder()
                .items(items)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    @Override
    public MediaUrlResponseDTO getMediaUrl(Long id) {
        MediaUploadEntity entity = mediaUploadRepository.findByIdAndStatus(id, MediaUploadStatusEnum.UPLOADED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));

        LocalDateTime expiresAt = LocalDateTime.now().plus(MEDIA_URL_TTL);

        return MediaUrlResponseDTO.builder()
                .id(entity.getId())
                .contentType(entity.getContentType())
                .objectKey(entity.getObjectKey())
                .url(presignMedia(entity.getObjectKey()))
                .thumbnailUrl(entity.getThumbnailObjectKey() != null ? presignMedia(entity.getThumbnailObjectKey()) : null)
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    public void deleteMedia(Long id) {
        MediaUploadEntity entity = mediaUploadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));

        deleteObjectIfPresent(entity.getObjectKey());
        deleteObjectIfPresent(entity.getThumbnailObjectKey());
        mediaUploadRepository.delete(entity);
    }

    @Override
    @Transactional
    public BulkDeleteResponseDTO bulkDeleteMedia(List<Long> ids) {
        List<BulkDeleteResponseDTO.BulkDeleteError> errors = new ArrayList<>();
        int successCount = 0;

        for (Long id : ids) {
            try {
                MediaUploadEntity entity = mediaUploadRepository.findById(id).orElse(null);
                if (entity == null) {
                    errors.add(BulkDeleteResponseDTO.BulkDeleteError.builder()
                            .id(id)
                            .filename("Unknown")
                            .reason("Media not found")
                            .build());
                    continue;
                }

                deleteObjectIfPresent(entity.getObjectKey());
                deleteObjectIfPresent(entity.getThumbnailObjectKey());
                mediaUploadRepository.delete(entity);
                successCount++;
            } catch (Exception e) {
                errors.add(BulkDeleteResponseDTO.BulkDeleteError.builder()
                        .id(id)
                        .filename("Unknown")
                        .reason(e.getMessage())
                        .build());
            }
        }

        return BulkDeleteResponseDTO.builder()
                .totalRequested(ids.size())
                .successCount(successCount)
                .failedCount(errors.size())
                .errors(errors)
                .build();
    }

    @Override
    public MediaListItemDTO updateTags(Long id, List<String> tags) {
        MediaUploadEntity entity = mediaUploadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));

        entity.setTags(tags != null ? tags : new ArrayList<>());
        mediaUploadRepository.save(entity);

        return toListItem(entity);
    }

    @Override
    public List<String> getAllTags() {
        return mediaUploadRepository.findAll().stream()
                .filter(e -> e.getTags() != null)
                .flatMap(e -> e.getTags().stream())
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public MediaListItemDTO renameMedia(Long id, String filename) {
        MediaUploadEntity entity = mediaUploadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));

        if (filename != null && !filename.isBlank()) {
            entity.setOriginalFilename(filename.trim());
            mediaUploadRepository.save(entity);
        }

        return toListItem(entity);
    }

    private MediaListItemDTO toListItem(MediaUploadEntity entity) {
        return MediaListItemDTO.builder()
                .id(entity.getId())
                .contentType(entity.getContentType())
                .objectKey(entity.getObjectKey())
                .thumbnailObjectKey(entity.getThumbnailObjectKey())
                .thumbnailUrl(entity.getThumbnailObjectKey() != null ? presignMedia(entity.getThumbnailObjectKey()) : null)
                .url(presignMedia(entity.getObjectKey()))
                .originalFilename(entity.getOriginalFilename())
                .mimeType(entity.getMimeType())
                .uploadedSize(entity.getUploadedSize())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .durationInSeconds(entity.getDurationInSeconds())
                .createdAt(entity.getCreatedAt())
                .tags(entity.getTags())
                .build();
    }

    private Set<Long> getExcludedImageIds() {
        Set<Long> excludedIds = new HashSet<>();
        excludedIds.addAll(userRepository.findAllProfileImageIds());
        excludedIds.addAll(organizationRepository.findAllLogoImageIds());
        return excludedIds;
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

    private void deleteObjectIfPresent(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build());
    }

    private String getSafeFolder(ContentTypeEnum contentType, String folder) {
        if (folder != null) {
            return switch (folder) {
                case "profile-images", "logos", "organization_logos" -> folder;
                default -> getDefaultFolder(contentType);
            };
        }
        return getDefaultFolder(contentType);
    }

    private String getDefaultFolder(ContentTypeEnum contentType) {
        return switch (contentType) {
            case VIDEO -> "videos";
            case AUDIO -> "audio";
            default -> "images";
        };
    }

    private String getExtension(String filename, String mimeType) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            case "video/mp4" -> ".mp4";
            case "video/webm" -> ".webm";
            case "video/quicktime" -> ".mov";
            case "video/x-msvideo" -> ".avi";
            case "audio/mpeg", "audio/mp3" -> ".mp3";
            case "audio/wav", "audio/x-wav" -> ".wav";
            case "audio/ogg" -> ".ogg";
            case "audio/aac" -> ".aac";
            case "audio/flac" -> ".flac";
            case "audio/m4a", "audio/x-m4a" -> ".m4a";
            default -> ".bin";
        };
    }
}
