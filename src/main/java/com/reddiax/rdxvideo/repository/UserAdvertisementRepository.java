package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.constant.UserAdvertisementStatusEnum;
import com.reddiax.rdxvideo.model.entity.UserAdvertisementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAdvertisementRepository extends JpaRepository<UserAdvertisementEntity, Long> {

    // Find by user
    List<UserAdvertisementEntity> findByUserId(Long userId);
    
    Page<UserAdvertisementEntity> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT ua FROM UserAdvertisementEntity ua WHERE ua.user.externalId = :externalId")
    List<UserAdvertisementEntity> findByUserExternalId(@Param("externalId") String externalId);

    // Find active ad for user
    @Query("SELECT ua FROM UserAdvertisementEntity ua WHERE ua.user.externalId = :externalId AND ua.status = 'ACTIVE'")
    Optional<UserAdvertisementEntity> findActiveByUserExternalId(@Param("externalId") String externalId);
    
    // Find active or paused ad for user (for mobile app display)
    @Query("SELECT ua FROM UserAdvertisementEntity ua WHERE ua.user.externalId = :externalId AND ua.status IN ('ACTIVE', 'PAUSED') ORDER BY ua.status ASC")
    Optional<UserAdvertisementEntity> findActiveOrPausedByUserExternalId(@Param("externalId") String externalId);
    
    // Find ALL active or paused ads for user (for cancelling when activating new ad)
    @Query("SELECT ua FROM UserAdvertisementEntity ua WHERE ua.user.externalId = :externalId AND ua.status IN ('ACTIVE', 'PAUSED')")
    List<UserAdvertisementEntity> findAllActiveOrPausedByUserExternalId(@Param("externalId") String externalId);
    
    Optional<UserAdvertisementEntity> findByUserIdAndStatus(Long userId, UserAdvertisementStatusEnum status);
    
    // Find paused ad for user
    @Query("SELECT ua FROM UserAdvertisementEntity ua WHERE ua.user.externalId = :externalId AND ua.advertisement.id = :advertisementId AND ua.status = :status")
    Optional<UserAdvertisementEntity> findByUserExternalIdAndAdvertisementIdAndStatus(
        @Param("externalId") String externalId, 
        @Param("advertisementId") Long advertisementId, 
        @Param("status") UserAdvertisementStatusEnum status);
    
    // Find user_advertisement by id and verify ownership
    @Query("SELECT ua FROM UserAdvertisementEntity ua WHERE ua.id = :id AND ua.user.externalId = :externalId")
    Optional<UserAdvertisementEntity> findByIdAndUserExternalId(
        @Param("id") Long id, 
        @Param("externalId") String externalId);
    
    // Find user_advertisement by id, user and status
    @Query("SELECT ua FROM UserAdvertisementEntity ua WHERE ua.id = :id AND ua.user.externalId = :externalId AND ua.status = :status")
    Optional<UserAdvertisementEntity> findByIdAndUserExternalIdAndStatus(
        @Param("id") Long id, 
        @Param("externalId") String externalId, 
        @Param("status") UserAdvertisementStatusEnum status);
    
    // Count by user and status
    Long countByUserIdAndStatus(Long userId, UserAdvertisementStatusEnum status);
    
    // Count all ads for a user
    Long countByUserId(Long userId);

    // Find by advertisement
    List<UserAdvertisementEntity> findByAdvertisementId(Long advertisementId);
    
    Page<UserAdvertisementEntity> findByAdvertisementId(Long advertisementId, Pageable pageable);

    // Check if user already has this ad
    boolean existsByUserIdAndAdvertisementId(Long userId, Long advertisementId);
    
    @Query("SELECT COUNT(ua) > 0 FROM UserAdvertisementEntity ua WHERE ua.user.externalId = :externalId AND ua.advertisement.id = :advertisementId")
    boolean existsByUserExternalIdAndAdvertisementId(@Param("externalId") String externalId, @Param("advertisementId") Long advertisementId);
    
    // Find existing user_advertisement by user and advertisement (regardless of status)
    @Query("SELECT ua FROM UserAdvertisementEntity ua WHERE ua.user.id = :userId AND ua.advertisement.id = :advertisementId")
    Optional<UserAdvertisementEntity> findByUserIdAndAdvertisementId(@Param("userId") Long userId, @Param("advertisementId") Long advertisementId);

    // Analytics queries
    
    // Count active users for an ad
    @Query("SELECT COUNT(ua) FROM UserAdvertisementEntity ua WHERE ua.advertisement.id = :advertisementId AND ua.status = 'ACTIVE'")
    Long countActiveUsersByAdvertisementId(@Param("advertisementId") Long advertisementId);
    
    // Count total users for an ad (all statuses)
    @Query("SELECT COUNT(ua) FROM UserAdvertisementEntity ua WHERE ua.advertisement.id = :advertisementId")
    Long countTotalUsersByAdvertisementId(@Param("advertisementId") Long advertisementId);
    
    // Count completed users for an ad
    @Query("SELECT COUNT(ua) FROM UserAdvertisementEntity ua WHERE ua.advertisement.id = :advertisementId AND ua.status = 'COMPLETED'")
    Long countCompletedUsersByAdvertisementId(@Param("advertisementId") Long advertisementId);
    
    // Count violated users for an ad
    @Query("SELECT COUNT(ua) FROM UserAdvertisementEntity ua WHERE ua.advertisement.id = :advertisementId AND ua.status = 'VIOLATED'")
    Long countViolatedUsersByAdvertisementId(@Param("advertisementId") Long advertisementId);

    // Global analytics
    
    // Total active users across all ads
    @Query("SELECT COUNT(ua) FROM UserAdvertisementEntity ua WHERE ua.status = 'ACTIVE'")
    Long countAllActiveUsers();
    
    // Total users who have ever shown an ad
    @Query("SELECT COUNT(DISTINCT ua.user.id) FROM UserAdvertisementEntity ua")
    Long countDistinctUsers();
    
    // Count by status
    Long countByStatus(UserAdvertisementStatusEnum status);

    // Find expired active ads (for batch processing)
    @Query("SELECT ua FROM UserAdvertisementEntity ua WHERE ua.status = 'ACTIVE' AND ua.expiresAt < :now")
    List<UserAdvertisementEntity> findExpiredActiveAds(@Param("now") LocalDateTime now);

    // Find ads that haven't been verified recently (potential violations)
    @Query("SELECT ua FROM UserAdvertisementEntity ua WHERE ua.status = 'ACTIVE' AND ua.lastVerifiedAt < :threshold")
    List<UserAdvertisementEntity> findStaleActiveAds(@Param("threshold") LocalDateTime threshold);

    // Analytics by organization
    @Query("SELECT COUNT(ua) FROM UserAdvertisementEntity ua WHERE ua.advertisement.organizationId = :organizationId AND ua.status = 'ACTIVE'")
    Long countActiveUsersByOrganizationId(@Param("organizationId") Long organizationId);

    @Query("SELECT COUNT(DISTINCT ua.user.id) FROM UserAdvertisementEntity ua WHERE ua.advertisement.organizationId = :organizationId")
    Long countDistinctUsersByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Check if there are any active promotions for an advertisement
    @Query("SELECT COUNT(ua) > 0 FROM UserAdvertisementEntity ua WHERE ua.advertisement.id = :advertisementId AND ua.status = 'ACTIVE'")
    boolean existsActiveByAdvertisementId(@Param("advertisementId") Long advertisementId);
    
    // Get all user advertisement IDs for an advertisement (for cascade delete)
    @Query("SELECT ua.id FROM UserAdvertisementEntity ua WHERE ua.advertisement.id = :advertisementId")
    List<Long> findIdsByAdvertisementId(@Param("advertisementId") Long advertisementId);
    
    // Delete all user advertisements for an advertisement
    void deleteByAdvertisementId(Long advertisementId);
}
