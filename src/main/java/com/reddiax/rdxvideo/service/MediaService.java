package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.constant.ContentTypeEnum;
import com.reddiax.rdxvideo.model.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MediaService {
    MediaUploadLinkResponse getUploadLink(ContentTypeEnum contentType, String filename, String mimeType, Long expectedSize, String folder);

    MediaUploadCompleteResponse uploadComplete(MediaUploadCompleteRequest request);

    MediaPageResponseDTO listMedia(Pageable pageable, ContentTypeEnum contentType, String folder, String search, List<String> tags);

    MediaUrlResponseDTO getMediaUrl(Long id);

    void deleteMedia(Long id);

    BulkDeleteResponseDTO bulkDeleteMedia(List<Long> ids);

    MediaListItemDTO updateTags(Long id, List<String> tags);

    List<String> getAllTags();

    MediaListItemDTO renameMedia(Long id, String filename);
}
