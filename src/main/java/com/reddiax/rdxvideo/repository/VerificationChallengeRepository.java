package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.VerificationChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationChallengeRepository extends JpaRepository<VerificationChallengeEntity, Long> {
    
    Optional<VerificationChallengeEntity> findByNonce(String nonce);
    
    List<VerificationChallengeEntity> findByDeviceId(Long deviceId);
    
    @Query("SELECT c FROM VerificationChallengeEntity c WHERE c.device.id = :deviceId " +
           "AND c.respondedAt IS NULL AND c.expiresAt > :now ORDER BY c.issuedAt DESC")
    List<VerificationChallengeEntity> findPendingChallengesByDevice(
            @Param("deviceId") Long deviceId,
            @Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM VerificationChallengeEntity c WHERE c.device.id = :deviceId " +
           "AND c.respondedAt IS NULL AND c.expiresAt > :now ORDER BY c.issuedAt DESC LIMIT 1")
    Optional<VerificationChallengeEntity> findLatestPendingChallengeByDevice(
            @Param("deviceId") Long deviceId,
            @Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM VerificationChallengeEntity c WHERE c.device.deviceToken = :deviceToken " +
           "AND c.respondedAt IS NULL AND c.expiresAt > :now ORDER BY c.issuedAt DESC LIMIT 1")
    Optional<VerificationChallengeEntity> findLatestPendingChallengeByDeviceToken(
            @Param("deviceToken") String deviceToken,
            @Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM VerificationChallengeEntity c WHERE c.userAdvertisement.id = :userAdId " +
           "ORDER BY c.issuedAt DESC")
    List<VerificationChallengeEntity> findByUserAdvertisementId(@Param("userAdId") Long userAdId);
    
    @Query("SELECT COUNT(c) FROM VerificationChallengeEntity c WHERE c.device.id = :deviceId " +
           "AND c.responseValid = false AND c.respondedAt > :since")
    long countFailedChallengesSince(
            @Param("deviceId") Long deviceId,
            @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(c) FROM VerificationChallengeEntity c WHERE c.device.id = :deviceId " +
           "AND c.wallpaperHashMatch = false AND c.respondedAt > :since")
    long countHashMismatchesSince(
            @Param("deviceId") Long deviceId,
            @Param("since") LocalDateTime since);
    
    @Query("DELETE FROM VerificationChallengeEntity c WHERE c.expiresAt < :before")
    void deleteExpiredChallenges(@Param("before") LocalDateTime before);
    
    void deleteByUserAdvertisementId(Long userAdvertisementId);
    
    @Query("DELETE FROM VerificationChallengeEntity c WHERE c.userAdvertisement.id IN :userAdIds")
    void deleteByUserAdvertisementIdIn(@Param("userAdIds") List<Long> userAdIds);
}
