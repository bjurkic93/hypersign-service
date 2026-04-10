package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.AdvertisementVerificationLogEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdvertisementVerificationLogRepository extends JpaRepository<AdvertisementVerificationLogEntity, Long> {

    /**
     * Find all logs for a user advertisement.
     */
    List<AdvertisementVerificationLogEntity> findByUserAdvertisementIdOrderByVerifiedAtDesc(Long userAdvertisementId);

    /**
     * Find logs for a user advertisement within a time range.
     */
    @Query("SELECT v FROM AdvertisementVerificationLogEntity v " +
           "WHERE v.userAdvertisement.id = :userAdvertisementId " +
           "AND v.verifiedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY v.verifiedAt ASC")
    List<AdvertisementVerificationLogEntity> findByUserAdvertisementIdAndDateRange(
            @Param("userAdvertisementId") Long userAdvertisementId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find logs for an advertisement (across all users) within a time range.
     */
    @Query("SELECT v FROM AdvertisementVerificationLogEntity v " +
           "WHERE v.userAdvertisement.advertisement.id = :advertisementId " +
           "AND v.verifiedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY v.verifiedAt ASC")
    List<AdvertisementVerificationLogEntity> findByAdvertisementIdAndDateRange(
            @Param("advertisementId") Long advertisementId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count verifications per hour for timeline chart.
     */
    @Query(value = "SELECT DATE_TRUNC('hour', v.verified_at) as hour, " +
                   "COUNT(*) as total_count, " +
                   "SUM(CASE WHEN v.hash_match = true THEN 1 ELSE 0 END) as success_count, " +
                   "SUM(CASE WHEN v.hash_match = false THEN 1 ELSE 0 END) as fail_count, " +
                   "COUNT(DISTINCT v.device_registration_id) as unique_devices " +
                   "FROM advertisement_verification_log v " +
                   "JOIN user_advertisement ua ON v.user_advertisement_id = ua.id " +
                   "WHERE ua.advertisement_id = :advertisementId " +
                   "AND v.verified_at BETWEEN :startDate AND :endDate " +
                   "GROUP BY DATE_TRUNC('hour', v.verified_at) " +
                   "ORDER BY hour ASC",
           nativeQuery = true)
    List<Object[]> getHourlyTimelineByAdvertisement(
            @Param("advertisementId") Long advertisementId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count verifications per day for timeline chart.
     */
    @Query(value = "SELECT DATE_TRUNC('day', v.verified_at) as day, " +
                   "COUNT(*) as total_count, " +
                   "SUM(CASE WHEN v.hash_match = true THEN 1 ELSE 0 END) as success_count, " +
                   "SUM(CASE WHEN v.hash_match = false THEN 1 ELSE 0 END) as fail_count, " +
                   "COUNT(DISTINCT v.device_registration_id) as unique_devices " +
                   "FROM advertisement_verification_log v " +
                   "JOIN user_advertisement ua ON v.user_advertisement_id = ua.id " +
                   "WHERE ua.advertisement_id = :advertisementId " +
                   "AND v.verified_at BETWEEN :startDate AND :endDate " +
                   "GROUP BY DATE_TRUNC('day', v.verified_at) " +
                   "ORDER BY day ASC",
           nativeQuery = true)
    List<Object[]> getDailyTimelineByAdvertisement(
            @Param("advertisementId") Long advertisementId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get per-device analytics for an advertisement.
     */
    @Query(value = "SELECT ua.id as user_ad_id, " +
                   "ua.device_info as device_info, " +
                   "COUNT(v.id) as total_verifications, " +
                   "COALESCE(SUM(v.duration_seconds), 0) as total_seconds, " +
                   "MAX(v.verified_at) as last_verified_at " +
                   "FROM user_advertisement ua " +
                   "LEFT JOIN advertisement_verification_log v ON v.user_advertisement_id = ua.id " +
                   "WHERE ua.advertisement_id = :advertisementId " +
                   "GROUP BY ua.id, ua.device_info " +
                   "ORDER BY total_seconds DESC",
           nativeQuery = true)
    List<Object[]> getDeviceAnalyticsByAdvertisement(@Param("advertisementId") Long advertisementId, Pageable pageable);

    /**
     * Get summary statistics for an advertisement.
     * Returns a List with single Object[] row containing: [total_display_time, active_devices, total_verifications, successful_verifications]
     */
    @Query(value = "SELECT " +
                   "COALESCE(SUM(v.duration_seconds), 0) as total_display_time, " +
                   "COUNT(DISTINCT ua.id) as active_devices, " +
                   "COUNT(v.id) as total_verifications, " +
                   "SUM(CASE WHEN v.hash_match = true THEN 1 ELSE 0 END) as successful_verifications " +
                   "FROM user_advertisement ua " +
                   "LEFT JOIN advertisement_verification_log v ON v.user_advertisement_id = ua.id " +
                   "WHERE ua.advertisement_id = :advertisementId",
           nativeQuery = true)
    List<Object[]> getAdvertisementSummary(@Param("advertisementId") Long advertisementId);

    /**
     * Count total verifications for an advertisement.
     */
    @Query("SELECT COUNT(v) FROM AdvertisementVerificationLogEntity v " +
           "WHERE v.userAdvertisement.advertisement.id = :advertisementId")
    Long countByAdvertisementId(@Param("advertisementId") Long advertisementId);

    /**
     * Sum total display time for an advertisement.
     */
    @Query("SELECT COALESCE(SUM(v.durationSeconds), 0) FROM AdvertisementVerificationLogEntity v " +
           "WHERE v.userAdvertisement.advertisement.id = :advertisementId")
    Long sumDurationSecondsByAdvertisementId(@Param("advertisementId") Long advertisementId);

    /**
     * Delete old logs for cleanup.
     */
    void deleteByVerifiedAtBefore(LocalDateTime cutoffDate);
}
