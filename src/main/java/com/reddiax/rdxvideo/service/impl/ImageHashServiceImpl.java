package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.constant.MediaUploadStatusEnum;
import com.reddiax.rdxvideo.model.entity.MediaUploadEntity;
import com.reddiax.rdxvideo.repository.MediaUploadRepository;
import com.reddiax.rdxvideo.service.ImageHashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.InputStream;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageHashServiceImpl implements ImageHashService {

    private final MediaUploadRepository mediaUploadRepository;
    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Override
    public String calculateImageHash(Long imageId) {
        return mediaUploadRepository.findById(imageId)
                .filter(media -> media.getStatus() == MediaUploadStatusEnum.UPLOADED)
                .map(MediaUploadEntity::getObjectKey)
                .map(this::calculateHashFromObjectKey)
                .orElse(null);
    }

    @Override
    public String calculateHashFromObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        try {
            log.debug("Calculating hash for S3 object: {}", objectKey);
            
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {
                String hash = calculateSha256(response);
                log.debug("Calculated hash for {}: {}", objectKey, hash);
                return hash;
            }
        } catch (Exception e) {
            log.error("Failed to calculate hash for object {}: {}", objectKey, e.getMessage());
            return null;
        }
    }

    private String calculateSha256(InputStream inputStream) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }
        
        byte[] hashBytes = digest.digest();
        return bytesToHex(hashBytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
