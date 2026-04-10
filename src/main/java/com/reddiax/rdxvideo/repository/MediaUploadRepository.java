package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.constant.ContentTypeEnum;
import com.reddiax.rdxvideo.constant.MediaUploadStatusEnum;
import com.reddiax.rdxvideo.model.entity.MediaUploadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaUploadRepository extends JpaRepository<MediaUploadEntity, Long>, JpaSpecificationExecutor<MediaUploadEntity> {
    Optional<MediaUploadEntity> findByObjectKey(String objectKey);
    
    Optional<MediaUploadEntity> findByIdAndContentType(Long id, ContentTypeEnum contentType);
    
    Optional<MediaUploadEntity> findByIdAndStatus(Long id, MediaUploadStatusEnum status);
}
