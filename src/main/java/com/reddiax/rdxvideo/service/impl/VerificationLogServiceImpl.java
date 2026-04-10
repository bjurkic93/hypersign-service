package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.model.dto.VerificationLogItemDTO;
import com.reddiax.rdxvideo.model.dto.VerificationLogSyncRequestDTO;
import com.reddiax.rdxvideo.model.dto.VerificationLogSyncResponseDTO;
import com.reddiax.rdxvideo.model.entity.AdvertisementVerificationLogEntity;
import com.reddiax.rdxvideo.model.entity.DeviceRegistrationEntity;
import com.reddiax.rdxvideo.model.entity.UserAdvertisementEntity;
import com.reddiax.rdxvideo.repository.AdvertisementVerificationLogRepository;
import com.reddiax.rdxvideo.repository.DeviceRegistrationRepository;
import com.reddiax.rdxvideo.repository.UserAdvertisementRepository;
import com.reddiax.rdxvideo.service.VerificationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationLogServiceImpl implements VerificationLogService {

    private final AdvertisementVerificationLogRepository verificationLogRepository;
    private final UserAdvertisementRepository userAdvertisementRepository;
    private final DeviceRegistrationRepository deviceRegistrationRepository;

    @Override
    @Transactional
    public VerificationLogSyncResponseDTO syncLogs(Long userId, VerificationLogSyncRequestDTO request) {
        List<VerificationLogItemDTO> logs = request.getLogs();
        
        if (logs == null || logs.isEmpty()) {
            return VerificationLogSyncResponseDTO.builder()
                    .syncedCount(0)
                    .message("No logs to sync")
                    .build();
        }
        
        // Try to find device registration if device token is provided
        DeviceRegistrationEntity deviceRegistration = null;
        if (request.getDeviceToken() != null && !request.getDeviceToken().isEmpty()) {
            deviceRegistration = deviceRegistrationRepository
                    .findByDeviceToken(request.getDeviceToken())
                    .orElse(null);
        }
        
        List<AdvertisementVerificationLogEntity> entitiesToSave = new ArrayList<>();
        Long lastTimestamp = null;
        int syncedCount = 0;
        int skippedCount = 0;
        
        for (VerificationLogItemDTO logItem : logs) {
            try {
                // Validate user_advertisement_id belongs to this user
                Optional<UserAdvertisementEntity> userAdOpt = 
                        userAdvertisementRepository.findById(logItem.getUserAdvertisementId());
                
                if (userAdOpt.isEmpty()) {
                    log.warn("UserAdvertisement not found: {}", logItem.getUserAdvertisementId());
                    skippedCount++;
                    continue;
                }
                
                UserAdvertisementEntity userAd = userAdOpt.get();
                
                // Verify ownership
                if (!userAd.getUser().getId().equals(userId)) {
                    log.warn("User {} doesn't own UserAdvertisement {}", userId, logItem.getUserAdvertisementId());
                    skippedCount++;
                    continue;
                }
                
                // Convert timestamp to LocalDateTime
                LocalDateTime verifiedAt = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(logItem.getTimestamp()),
                        ZoneId.systemDefault()
                );
                
                // Calculate duration seconds (estimate based on 15-minute intervals)
                int durationSeconds = 15 * 60; // Default to 15 minutes
                
                // Create entity
                AdvertisementVerificationLogEntity entity = AdvertisementVerificationLogEntity.builder()
                        .userAdvertisement(userAd)
                        .deviceRegistration(deviceRegistration)
                        .verifiedAt(verifiedAt)
                        .wallpaperHash(logItem.getWallpaperHash())
                        .hashMatch(logItem.getHashMatch() != null ? logItem.getHashMatch() : false)
                        .verificationResult(logItem.getVerificationResult() != null ? logItem.getVerificationResult() : "UNKNOWN")
                        .durationSeconds(durationSeconds)
                        .deviceState(logItem.getDeviceState())
                        .createdAt(LocalDateTime.now())
                        .build();
                
                entitiesToSave.add(entity);
                
                // Track last timestamp
                if (lastTimestamp == null || logItem.getTimestamp() > lastTimestamp) {
                    lastTimestamp = logItem.getTimestamp();
                }
                
                syncedCount++;
                
            } catch (Exception e) {
                log.error("Error processing verification log item: {}", logItem, e);
                skippedCount++;
            }
        }
        
        // Save all entities in batch
        if (!entitiesToSave.isEmpty()) {
            verificationLogRepository.saveAll(entitiesToSave);
            log.info("Synced {} verification logs for user {}, skipped {}", syncedCount, userId, skippedCount);
        }
        
        return VerificationLogSyncResponseDTO.builder()
                .syncedCount(syncedCount)
                .lastSyncedTimestamp(lastTimestamp)
                .message(skippedCount > 0 
                        ? String.format("Synced %d logs, skipped %d", syncedCount, skippedCount)
                        : String.format("Successfully synced %d logs", syncedCount))
                .build();
    }
}
