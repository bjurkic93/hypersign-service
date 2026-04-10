package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.constant.ContentTypeEnum;
import com.reddiax.rdxvideo.model.dto.*;
import com.reddiax.rdxvideo.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/media")
@Tag(name = "Media Management", description = "Unified API for managing images and videos")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @GetMapping("/upload-link")
    @Operation(summary = "Get presigned upload URL", description = "Generate a presigned URL for uploading a media file (image or video)")
    public MediaUploadLinkResponse getUploadLink(
            @RequestParam ContentTypeEnum contentType,
            @RequestParam String filename,
            @RequestParam String mimeType,
            @RequestParam Long expectedSize,
            @RequestParam(required = false) String folder,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} requesting {} upload link for file: {} in folder: {}", 
                jwt.getSubject(), contentType, filename, folder);
        return mediaService.getUploadLink(contentType, filename, mimeType, expectedSize, folder);
    }

    @PostMapping("/upload-complete")
    @Operation(summary = "Complete upload", description = "Mark a media upload as complete after the file has been uploaded")
    public MediaUploadCompleteResponse uploadComplete(
            @RequestBody MediaUploadCompleteRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} marking media upload complete: {}", jwt.getSubject(), request.getObjectKey());
        return mediaService.uploadComplete(request);
    }

    @GetMapping
    @Operation(summary = "List media", description = "Get a paginated list of media. Filter by contentType (IMAGE, VIDEO), folder, search term, or tags")
    public MediaPageResponseDTO listMedia(
            @PageableDefault(size = 20, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) ContentTypeEnum contentType,
            @RequestParam(required = false) String folder,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> tags,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} listing media, contentType: {}, folder: {}, search: {}, tags: {}, page: {}", 
                jwt.getSubject(), contentType, folder, search, tags, pageable.getPageNumber());
        return mediaService.listMedia(pageable, contentType, folder, search, tags);
    }

    @GetMapping("/{id}/url")
    @Operation(summary = "Get media URL", description = "Get a presigned URL for viewing/playing a media file")
    public MediaUrlResponseDTO getMediaUrl(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} requesting URL for media: {}", jwt.getSubject(), id);
        return mediaService.getMediaUrl(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete media", description = "Delete a media file by ID")
    public void deleteMedia(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} deleting media: {}", jwt.getSubject(), id);
        mediaService.deleteMedia(id);
    }

    @DeleteMapping("/bulk")
    @Operation(summary = "Bulk delete media", description = "Delete multiple media files by IDs")
    public BulkDeleteResponseDTO bulkDeleteMedia(
            @RequestBody List<Long> ids,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} bulk deleting {} media items", jwt.getSubject(), ids.size());
        return mediaService.bulkDeleteMedia(ids);
    }

    @PutMapping("/{id}/tags")
    @Operation(summary = "Update media tags", description = "Update the tags for a media item")
    public MediaListItemDTO updateTags(
            @PathVariable Long id,
            @RequestBody List<String> tags,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} updating tags for media {}: {}", jwt.getSubject(), id, tags);
        return mediaService.updateTags(id, tags);
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all tags", description = "Get a list of all unique tags used across media items")
    public List<String> getAllTags(@AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching all tags", jwt.getSubject());
        return mediaService.getAllTags();
    }

    @PutMapping("/{id}/rename")
    @Operation(summary = "Rename media", description = "Rename a media item")
    public MediaListItemDTO renameMedia(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> request,
            @AuthenticationPrincipal Jwt jwt) {
        String filename = request.get("filename");
        log.info("User {} renaming media {} to: {}", jwt.getSubject(), id, filename);
        return mediaService.renameMedia(id, filename);
    }
}
