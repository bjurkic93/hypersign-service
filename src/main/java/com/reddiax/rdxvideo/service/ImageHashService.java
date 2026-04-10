package com.reddiax.rdxvideo.service;

/**
 * Service for calculating image content hashes.
 */
public interface ImageHashService {
    
    /**
     * Calculate SHA-256 hash of an image stored in S3.
     * 
     * @param imageId The ID of the image in the database
     * @return SHA-256 hash as hex string, or null if calculation fails
     */
    String calculateImageHash(Long imageId);
    
    /**
     * Calculate SHA-256 hash of image content from S3 object key.
     * 
     * @param objectKey The S3 object key
     * @return SHA-256 hash as hex string, or null if calculation fails
     */
    String calculateHashFromObjectKey(String objectKey);
}
