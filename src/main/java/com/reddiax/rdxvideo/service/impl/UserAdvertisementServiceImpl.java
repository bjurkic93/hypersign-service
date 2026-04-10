package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.constant.UserAdvertisementStatusEnum;
import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.*;
import com.reddiax.rdxvideo.model.entity.*;
import com.reddiax.rdxvideo.repository.*;
import com.reddiax.rdxvideo.service.MediaService;
import com.reddiax.rdxvideo.service.UserAdvertisementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAdvertisementServiceImpl implements UserAdvertisementService {

    private final UserAdvertisementRepository userAdvertisementRepository;
    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementImageRepository advertisementImageRepository;
    private final MediaService mediaService;

    // ==================== CMS/Admin Endpoints ====================

    @Override
    @Transactional(readOnly = true)
    public Page<UserAdvertisementDTO> getUsersByAdvertisement(Long advertisementId, Pageable pageable) {
        return userAdvertisementRepository.findByAdvertisementId(advertisementId, pageable)
            .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AdvertisementAnalyticsDTO getAdvertisementAnalytics(Long advertisementId) {
        AdvertisementEntity ad = advertisementRepository.findById(advertisementId)
            .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                "Advertisement not found: " + advertisementId, "AD_NOT_FOUND"));
        
        Long activeUsers = userAdvertisementRepository.countActiveUsersByAdvertisementId(advertisementId);
        Long totalUsers = userAdvertisementRepository.countTotalUsersByAdvertisementId(advertisementId);
        Long completedUsers = userAdvertisementRepository.countCompletedUsersByAdvertisementId(advertisementId);
        Long violatedUsers = userAdvertisementRepository.countViolatedUsersByAdvertisementId(advertisementId);
        
        double completionRate = totalUsers > 0 ? (completedUsers.doubleValue() / totalUsers.doubleValue()) * 100 : 0;
        double violationRate = totalUsers > 0 ? (violatedUsers.doubleValue() / totalUsers.doubleValue()) * 100 : 0;
        
        return AdvertisementAnalyticsDTO.builder()
            .advertisementId(advertisementId)
            .advertisementName(ad.getName())
            .activeUsers(activeUsers)
            .totalUsers(totalUsers)
            .completedUsers(completedUsers)
            .violatedUsers(violatedUsers)
            .completionRate(completionRate)
            .violationRate(violationRate)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalAnalyticsDTO getGlobalAnalytics() {
        Long activeUsers = userAdvertisementRepository.countAllActiveUsers();
        Long uniqueUsers = userAdvertisementRepository.countDistinctUsers();
        Long completedSessions = userAdvertisementRepository.countByStatus(UserAdvertisementStatusEnum.COMPLETED);
        Long violatedSessions = userAdvertisementRepository.countByStatus(UserAdvertisementStatusEnum.VIOLATED);
        Long activeSessions = userAdvertisementRepository.countByStatus(UserAdvertisementStatusEnum.ACTIVE);
        
        long totalSessions = completedSessions + violatedSessions + activeSessions;
        double completionRate = totalSessions > 0 ? (completedSessions.doubleValue() / totalSessions) * 100 : 0;
        double violationRate = totalSessions > 0 ? (violatedSessions.doubleValue() / totalSessions) * 100 : 0;
        
        return GlobalAnalyticsDTO.builder()
            .totalActiveUsers(activeUsers)
            .totalUniqueUsers(uniqueUsers)
            .totalCompletedSessions(completedSessions)
            .totalViolatedSessions(violatedSessions)
            .totalActiveSessions(activeSessions)
            .overallCompletionRate(completionRate)
            .overallViolationRate(violationRate)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalAnalyticsDTO getOrganizationAnalytics(Long organizationId) {
        Long activeUsers = userAdvertisementRepository.countActiveUsersByOrganizationId(organizationId);
        Long uniqueUsers = userAdvertisementRepository.countDistinctUsersByOrganizationId(organizationId);
        
        return GlobalAnalyticsDTO.builder()
            .totalActiveUsers(activeUsers)
            .totalUniqueUsers(uniqueUsers)
            .build();
    }

    // ==================== Background Jobs ====================

    @Override
    @Transactional
    public void processExpiredAdvertisements() {
        List<UserAdvertisementEntity> expiredAds = 
            userAdvertisementRepository.findExpiredActiveAds(LocalDateTime.now());
        
        for (UserAdvertisementEntity userAd : expiredAds) {
            userAd.setStatus(UserAdvertisementStatusEnum.COMPLETED);
            userAd.setDeactivatedAt(LocalDateTime.now());
            userAdvertisementRepository.save(userAd);
            log.info("Auto-completed expired advertisement {} for user {}", 
                userAd.getAdvertisement().getId(), userAd.getUser().getId());
        }
        
        if (!expiredAds.isEmpty()) {
            log.info("Processed {} expired advertisements", expiredAds.size());
        }
    }

    @Override
    @Transactional
    public void processStaleAdvertisements() {
        // Mark ads as violated if not verified in last 24 hours
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<UserAdvertisementEntity> staleAds = 
            userAdvertisementRepository.findStaleActiveAds(threshold);
        
        for (UserAdvertisementEntity userAd : staleAds) {
            userAd.setStatus(UserAdvertisementStatusEnum.VIOLATED);
            userAd.setDeactivatedAt(LocalDateTime.now());
            userAd.setDeactivationReason("No verification for 24+ hours");
            userAdvertisementRepository.save(userAd);
            log.warn("Marked advertisement {} as violated for user {} - stale verification", 
                userAd.getAdvertisement().getId(), userAd.getUser().getId());
        }
        
        if (!staleAds.isEmpty()) {
            log.info("Marked {} stale advertisements as violated", staleAds.size());
        }
    }

    // ==================== Private Helpers ====================

    private UserAdvertisementDTO toDTO(UserAdvertisementEntity entity) {
        AdvertisementEntity ad = entity.getAdvertisement();
        
        // Resolve image URL from advertisement images
        String imageUrl = null;
        List<AdvertisementImageEntity> images = advertisementImageRepository.findByAdvertisementIdOrderByDisplayOrderAsc(ad.getId());
        if (!images.isEmpty()) {
            try {
                Long imageId = images.get(0).getImageId();
                imageUrl = mediaService.getMediaUrl(imageId).getUrl();
            } catch (Exception e) {
                log.warn("Failed to resolve image URL for ad {}", ad.getId());
            }
        }
        
        // Remaining time (until expires)
        long remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), entity.getExpiresAt());
        if (remainingSeconds < 0) remainingSeconds = 0;
        
        // Elapsed time
        long elapsedSeconds = ChronoUnit.SECONDS.between(entity.getActivatedAt(), LocalDateTime.now());
        if (elapsedSeconds < 0) elapsedSeconds = 0;
        
        return UserAdvertisementDTO.builder()
            .id(entity.getId())
            .userId(entity.getUser().getId())
            .advertisementId(ad.getId())
            .advertisementName(ad.getName())
            .advertisementImageUrl(imageUrl)
            .status(entity.getStatus())
            .activatedAt(entity.getActivatedAt())
            .expiresAt(entity.getExpiresAt())
            .deactivatedAt(entity.getDeactivatedAt())
            .verificationCount(entity.getVerificationCount())
            .lastVerifiedAt(entity.getLastVerifiedAt())
            .totalValidSeconds(entity.getTotalValidSeconds())
            .deviceInfo(entity.getDeviceInfo())
            .deactivationReason(entity.getDeactivationReason())
            .remainingSeconds(remainingSeconds)
            .elapsedSeconds(elapsedSeconds)
            .isExpired(entity.isExpired())
            .build();
    }
}
