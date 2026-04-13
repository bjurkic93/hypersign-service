package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.PlaybackLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlaybackLogRepository extends JpaRepository<PlaybackLogEntity, Long> {

    Page<PlaybackLogEntity> findByOrganizationIdOrderByStartedAtDesc(Long organizationId, Pageable pageable);

    Page<PlaybackLogEntity> findByDeviceIdOrderByStartedAtDesc(Long deviceId, Pageable pageable);

    List<PlaybackLogEntity> findByOrganizationIdAndStartedAtBetween(
            Long organizationId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT p.contentId, p.contentType, SUM(p.durationMs) as totalDuration, COUNT(p) as playCount " +
           "FROM PlaybackLogEntity p " +
           "WHERE p.organizationId = :orgId AND p.startedAt BETWEEN :start AND :end " +
           "GROUP BY p.contentId, p.contentType " +
           "ORDER BY totalDuration DESC")
    List<Object[]> getContentPlaybackStats(
            @Param("orgId") Long organizationId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT p.device.id, COUNT(p), SUM(p.durationMs) " +
           "FROM PlaybackLogEntity p " +
           "WHERE p.organizationId = :orgId AND p.startedAt BETWEEN :start AND :end " +
           "GROUP BY p.device.id")
    List<Object[]> getDevicePlaybackStats(
            @Param("orgId") Long organizationId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
